package pe.edu.ucv.hce.aplicacion;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ucv.hce.dominio.CodigoRol;
import pe.edu.ucv.hce.dominio.Establecimiento;
import pe.edu.ucv.hce.dominio.RegistroDuplicadoException;
import pe.edu.ucv.hce.dominio.Suscripcion;
import pe.edu.ucv.hce.dominio.Usuario;
import pe.edu.ucv.hce.persistencia.EstablecimientoRepository;
import pe.edu.ucv.hce.persistencia.RolRepository;
import pe.edu.ucv.hce.persistencia.SuscripcionRepository;
import pe.edu.ucv.hce.persistencia.UsuarioRepository;

/**
 * CU-16 Dar de alta un hospital (RF-20; RN-16, RN-14, RN-07). Solo el administrador de plataforma (RN-16).
 * Crea el establecimiento, su suscripcion y su administrador con clave temporal, que se devuelve UNA sola vez
 * (solo se guarda su hash) y debe cambiarse en el primer ingreso (RN-14, CU-18).
 */
@Service
public class PlataformaService {

    public static final String ACCION_ALTA_HOSPITAL = "ALTA_HOSPITAL";
    public static final List<String> PLANES = List.of("BASICO", "ESTANDAR", "COMPLETO");
    public static final int MESES_SUSCRIPCION = 12;

    /** Lo que vuelve del alta: la clave temporal solo existe aqui, en texto, para mostrarla una vez. */
    public record AltaHospital(Establecimiento hospital, Suscripcion suscripcion, Usuario administrador,
                               String claveTemporal) { }

    public record DatosHospital(String nombre, String codigo, String region, String plan,
                                String adminNombre, String adminDocumento, String adminCorreo, String adminUsuario) { }

    private final EstablecimientoRepository establecimientos;
    private final SuscripcionRepository suscripciones;
    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final AutorizacionService autorizacion;
    private final AuditoriaService auditoria;

    public PlataformaService(EstablecimientoRepository establecimientos, SuscripcionRepository suscripciones,
                             UsuarioRepository usuarios, RolRepository roles, AutorizacionService autorizacion,
                             AuditoriaService auditoria) {
        this.establecimientos = establecimientos;
        this.suscripciones = suscripciones;
        this.usuarios = usuarios;
        this.roles = roles;
        this.autorizacion = autorizacion;
        this.auditoria = auditoria;
    }

    @Transactional
    public AltaHospital altaHospital(Usuario solicitante, DatosHospital d, String ip) {
        autorizacion.exigir(solicitante, Permiso.GESTIONAR_HOSPITALES); // RN-16
        String plan = d.plan() == null ? "" : d.plan().trim().toUpperCase();
        if (!PLANES.contains(plan)) {
            throw new IllegalArgumentException("Elige un plan de suscripción válido.");
        }
        String codigo = d.codigo().trim();
        if (establecimientos.findByCodigoIpress(codigo).isPresent()) {
            throw new RegistroDuplicadoException("Ya existe un hospital con el código " + codigo + ".");
        }
        String nombreUsuario = d.adminUsuario().trim().toLowerCase();
        if (usuarios.findByUsuario(nombreUsuario).isPresent()) {
            throw new RegistroDuplicadoException("El usuario " + nombreUsuario + " ya existe. Elige otro.");
        }
        Establecimiento hospital = establecimientos.save(
            new Establecimiento(d.nombre().trim(), codigo, d.region().trim(), "ACTIVO"));
        LocalDate hoy = LocalDate.now();
        Suscripcion suscripcion = suscripciones.save(
            new Suscripcion(hospital, plan, hoy, hoy.plusMonths(MESES_SUSCRIPCION)));
        String clave = ClaveUtil.generarTemporal();
        Usuario admin = new Usuario(hospital, roles.findByNombre(CodigoRol.ADMIN_HOSPITAL.name()).orElseThrow(),
            nombreUsuario, ClaveUtil.bcrypt(clave), "ACTIVO");
        admin.setDatosPersona(d.adminNombre().trim(), d.adminDocumento().trim(), d.adminCorreo().trim());
        admin.setDebeCambiarClave(true); // RN-14
        admin = usuarios.save(admin);
        auditoria.registrar(solicitante, ACCION_ALTA_HOSPITAL, "establecimiento", hospital.getId(), null, ip);
        return new AltaHospital(hospital, suscripcion, admin, clave);
    }
}
