package pe.edu.ucv.hce.aplicacion;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ucv.hce.dominio.AccesoDenegadoException;
import pe.edu.ucv.hce.dominio.CodigoRol;
import pe.edu.ucv.hce.dominio.ProfesionalSalud;
import pe.edu.ucv.hce.dominio.RecursoNoEncontradoException;
import pe.edu.ucv.hce.dominio.RegistroDuplicadoException;
import pe.edu.ucv.hce.dominio.Usuario;
import pe.edu.ucv.hce.persistencia.ProfesionalSaludRepository;
import pe.edu.ucv.hce.persistencia.RolRepository;
import pe.edu.ucv.hce.persistencia.UsuarioRepository;

/**
 * CU-17 Gestionar usuarios del hospital (RF-11, RF-12; RN-16, RN-14, RN-12, RN-07, RN-06).
 * Solo el administrador del hospital, y solo sobre su propio hospital: el hospital sale de su sesion, nunca del cliente.
 * Roles que puede asignar: admision, enfermeria, medico y apoyo al diagnostico. No crea administradores (RN-16).
 * El personal clinico queda tambien como profesional de salud, con su numero de colegiatura.
 */
@Service
public class UsuariosHospitalService {

    public static final String ACCION_CREAR = "CREAR_USUARIO";
    public static final String ACCION_DESACTIVAR = "DESACTIVAR_USUARIO";
    public static final String ACCION_REACTIVAR = "REACTIVAR_USUARIO";
    public static final String ACTIVO = "ACTIVO";
    public static final String INACTIVO = "INACTIVO";

    /** Roles que un administrador del hospital puede asignar, con el tipo de profesional de salud que les toca. */
    public static final Map<CodigoRol, String> ROLES_ASIGNABLES = Map.of(
        CodigoRol.ADMISION, "",
        CodigoRol.ENFERMERIA, "ENFERMERIA",
        CodigoRol.MEDICO, "MEDICO",
        CodigoRol.APOYO_DIAGNOSTICO, "APOYO_DIAGNOSTICO");

    public record DatosUsuario(String nombres, String apellidos, String documento, String correo, String usuario,
                               String rol, String colegiatura, String especialidad) { }

    public record AltaUsuario(Usuario usuario, String claveTemporal) { }

    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final ProfesionalSaludRepository profesionales;
    private final AutorizacionService autorizacion;
    private final AuditoriaService auditoria;

    public UsuariosHospitalService(UsuarioRepository usuarios, RolRepository roles,
                                   ProfesionalSaludRepository profesionales, AutorizacionService autorizacion,
                                   AuditoriaService auditoria) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.profesionales = profesionales;
        this.autorizacion = autorizacion;
        this.auditoria = auditoria;
    }

    /** El personal del hospital del administrador (RN-06). */
    @Transactional(readOnly = true)
    public List<Usuario> listar(Usuario admin) {
        return usuarios.findAllByEstablecimientoIdOrderByUsuarioAsc(hospitalDe(admin));
    }

    /** Profesional de salud de un usuario (para mostrar su colegiatura), si lo es. */
    @Transactional(readOnly = true)
    public Optional<ProfesionalSalud> profesionalDe(Usuario u) {
        return profesionales.findByUsuarioId(u.getId());
    }

    /** CU-17: crea un usuario en el hospital del administrador, con clave temporal (RN-14) que se muestra una vez. */
    @Transactional
    public AltaUsuario crear(Usuario admin, DatosUsuario d, String ip) {
        Integer hospital = hospitalDe(admin);
        CodigoRol rol = rolAsignable(d.rol());
        boolean clinico = rol == CodigoRol.MEDICO || rol == CodigoRol.ENFERMERIA;
        String colegiatura = d.colegiatura() == null ? "" : d.colegiatura().trim();
        if (clinico && colegiatura.isEmpty()) {
            throw new IllegalArgumentException("Ingresa el número de colegiatura del personal clínico.");
        }
        String nombreUsuario = d.usuario().trim().toLowerCase();
        if (usuarios.findByUsuario(nombreUsuario).isPresent()) {
            throw new RegistroDuplicadoException("El usuario " + nombreUsuario + " ya existe. Elige otro.");
        }
        String clave = ClaveUtil.generarTemporal();
        Usuario nuevo = new Usuario(admin.getEstablecimiento(), roles.findByNombre(rol.name()).orElseThrow(),
            nombreUsuario, ClaveUtil.bcrypt(clave), ACTIVO);
        nuevo.setDatosPersona(d.apellidos().trim() + ", " + d.nombres().trim(), d.documento().trim(), d.correo().trim());
        nuevo.setDebeCambiarClave(true); // RN-14
        nuevo = usuarios.save(nuevo);
        String tipo = ROLES_ASIGNABLES.get(rol);
        if (!tipo.isEmpty()) {
            String especialidad = d.especialidad() == null || d.especialidad().isBlank()
                ? especialidadPorDefecto(rol) : d.especialidad().trim();
            profesionales.save(new ProfesionalSalud(nuevo, d.nombres().trim(), d.apellidos().trim(),
                colegiatura.isEmpty() ? "SIN-COLEGIATURA" : colegiatura, especialidad, tipo));
        }
        auditoria.registrar(admin, ACCION_CREAR, "usuario", nuevo.getId(), null, ip);
        return new AltaUsuario(nuevo, clave);
    }

    /** CU-17 paso 4: desactiva una cuenta de su hospital. Una cuenta inactiva no puede iniciar sesion. */
    @Transactional
    public Usuario desactivar(Usuario admin, Integer idUsuario, String ip) {
        Usuario u = deSuHospital(admin, idUsuario);
        if (u.getId().equals(admin.getId())) {
            throw new IllegalArgumentException("No puedes desactivar tu propia cuenta.");
        }
        u.setEstado(INACTIVO);
        auditoria.registrar(admin, ACCION_DESACTIVAR, "usuario", u.getId(), null, ip);
        return usuarios.save(u);
    }

    @Transactional
    public Usuario reactivar(Usuario admin, Integer idUsuario, String ip) {
        Usuario u = deSuHospital(admin, idUsuario);
        u.setEstado(ACTIVO);
        auditoria.registrar(admin, ACCION_REACTIVAR, "usuario", u.getId(), null, ip);
        return usuarios.save(u);
    }

    /** RN-16: un usuario de otro hospital se trata como inexistente. */
    private Usuario deSuHospital(Usuario admin, Integer idUsuario) {
        return usuarios.findByIdAndEstablecimientoId(idUsuario, hospitalDe(admin))
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario pedido en tu hospital."));
    }

    private Integer hospitalDe(Usuario admin) {
        autorizacion.exigir(admin, Permiso.GESTIONAR_USUARIOS);
        if (admin.getEstablecimiento() == null) {
            throw new AccesoDenegadoException("Tu usuario no pertenece a un hospital.");
        }
        return admin.getEstablecimiento().getId();
    }

    /** RN-16: solo los roles del personal del hospital; nunca administradores. */
    private CodigoRol rolAsignable(String rol) {
        CodigoRol codigo;
        try {
            codigo = CodigoRol.valueOf(rol == null ? "" : rol.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Elige un rol válido.");
        }
        if (!ROLES_ASIGNABLES.containsKey(codigo)) {
            throw new AccesoDenegadoException("El administrador del hospital no puede crear cuentas con ese rol.");
        }
        return codigo;
    }

    private static String especialidadPorDefecto(CodigoRol rol) {
        return switch (rol) {
            case MEDICO -> "Medicina general";
            case ENFERMERIA -> "Enfermería";
            default -> "Apoyo al diagnóstico";
        };
    }
}
