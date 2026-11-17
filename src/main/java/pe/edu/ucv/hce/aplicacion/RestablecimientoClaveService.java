package pe.edu.ucv.hce.aplicacion;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ucv.hce.dominio.AccesoDenegadoException;
import pe.edu.ucv.hce.dominio.CodigoRol;
import pe.edu.ucv.hce.dominio.HistoriaClinica;
import pe.edu.ucv.hce.dominio.RecursoNoEncontradoException;
import pe.edu.ucv.hce.dominio.Usuario;
import pe.edu.ucv.hce.persistencia.HistoriaClinicaRepository;
import pe.edu.ucv.hce.persistencia.UsuarioRepository;

/**
 * CU-26 Restablecer la clave de una cuenta (RN-14, RN-16, RN-06, RN-07; D-39).
 * Quien creo la cuenta puede darle una nueva clave temporal, que se muestra UNA sola vez (solo se guarda su hash),
 * obliga a cambiarla en el primer ingreso y levanta el bloqueo por intentos fallidos:
 * - el administrador del hospital, a su personal (admision, enfermeria, medico, apoyo);
 * - el administrador de plataforma, al administrador de un hospital;
 * - la admision, a los pacientes de su hospital (cuenta del portal).
 * Nadie restablece su propia clave por aqui (la cambia con CU-18).
 */
@Service
public class RestablecimientoClaveService {

    public static final String ACCION_RESTABLECER = "RESTABLECER_CLAVE";

    /** La clave temporal solo existe aqui, en texto, para mostrarla una vez. */
    public record ClaveRestablecida(Usuario cuenta, String claveTemporal) { }

    private final UsuarioRepository usuarios;
    private final HistoriaClinicaRepository historias;
    private final AutorizacionService autorizacion;
    private final AuditoriaService auditoria;

    public RestablecimientoClaveService(UsuarioRepository usuarios, HistoriaClinicaRepository historias,
                                        AutorizacionService autorizacion, AuditoriaService auditoria) {
        this.usuarios = usuarios;
        this.historias = historias;
        this.autorizacion = autorizacion;
        this.auditoria = auditoria;
    }

    /** Administrador del hospital sobre su personal (RN-16, RN-06). Otro hospital se trata como inexistente. */
    @Transactional
    public ClaveRestablecida restablecerPersonal(Usuario admin, Integer idUsuario, String ip) {
        autorizacion.exigir(admin, Permiso.GESTIONAR_USUARIOS);
        Usuario u = usuarios.findByIdAndEstablecimientoId(idUsuario, hospitalDe(admin))
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario pedido en tu hospital."));
        if (u.getId().equals(admin.getId())) {
            throw new IllegalArgumentException("No puedes restablecer tu propia clave; cámbiala desde tu perfil.");
        }
        if (!esPersonal(u)) {
            throw new AccesoDenegadoException("Solo puedes restablecer la clave del personal de tu hospital.");
        }
        return aplicar(admin, u, ip);
    }

    /** Administrador de plataforma sobre el administrador de un hospital (RN-16). */
    @Transactional
    public ClaveRestablecida restablecerAdministradorDeHospital(Usuario plataforma, Integer idHospital, String ip) {
        autorizacion.exigir(plataforma, Permiso.GESTIONAR_HOSPITALES);
        Usuario admin = usuarios.findFirstByEstablecimientoIdAndRolNombreOrderByIdAsc(
                idHospital, CodigoRol.ADMIN_HOSPITAL.name())
            .orElseThrow(() -> new RecursoNoEncontradoException("Ese hospital no tiene un administrador registrado."));
        return aplicar(plataforma, admin, ip);
    }

    /** Admision sobre la cuenta del portal de un paciente de su hospital (RN-06, RN-15). */
    @Transactional
    public ClaveRestablecida restablecerPaciente(Usuario admision, Integer idHistoria, String ip) {
        autorizacion.exigir(admision, Permiso.REGISTRAR_PACIENTE);
        Integer hospital = hospitalDe(admision);
        HistoriaClinica h = historias.findById(idHistoria)
            .filter(x -> x.getEstablecimiento().getId().equals(hospital))
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe la historia pedida en tu hospital."));
        Usuario cuenta = h.getPaciente().getUsuario();
        if (cuenta == null) {
            throw new RecursoNoEncontradoException("El paciente no tiene cuenta del portal.");
        }
        return aplicar(admision, cuenta, ip);
    }

    private ClaveRestablecida aplicar(Usuario quien, Usuario cuenta, String ip) {
        String clave = ClaveUtil.generarTemporal();
        cuenta.restablecerClave(ClaveUtil.bcrypt(clave), LocalDateTime.now()); // RN-14
        usuarios.save(cuenta);
        auditoria.registrar(quien, ACCION_RESTABLECER, "usuario", cuenta.getId(), null, ip); // RN-07
        return new ClaveRestablecida(cuenta, clave);
    }

    private static boolean esPersonal(Usuario u) {
        return UsuariosHospitalService.ROLES_ASIGNABLES.keySet().stream().anyMatch(u::tieneRol);
    }

    private static Integer hospitalDe(Usuario u) {
        if (u.getEstablecimiento() == null) {
            throw new AccesoDenegadoException("Tu usuario no pertenece a un hospital.");
        }
        return u.getEstablecimiento().getId();
    }
}
