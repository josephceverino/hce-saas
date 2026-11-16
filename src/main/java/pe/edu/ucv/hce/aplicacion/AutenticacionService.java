package pe.edu.ucv.hce.aplicacion;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ucv.hce.dominio.CuentaBloqueadaException;
import pe.edu.ucv.hce.dominio.NoAutenticadoException;
import pe.edu.ucv.hce.dominio.RegistroAuditoria;
import pe.edu.ucv.hce.dominio.Usuario;
import pe.edu.ucv.hce.persistencia.RegistroAuditoriaRepository;
import pe.edu.ucv.hce.persistencia.UsuarioRepository;

/**
 * CU-01: iniciar sesion (RF-10; RN-07, RN-12).
 * RN-12: claves con BCrypt y bloqueo temporal tras N intentos fallidos consecutivos (por defecto 5 en 15 minutos).
 * El bloqueo se calcula con los registros de auditoria (LOGIN_FALLIDO desde el ultimo INICIO_SESION): no necesita
 * columnas nuevas en usuario y se levanta solo pasado el tiempo, asi la demo no queda trabada sin un administrador.
 * Los hashes antiguos (SHA-256) se migran a BCrypt cuando el usuario entra con su clave correcta.
 */
@Service
public class AutenticacionService {

    public static final String ACCION_INICIO_SESION = "INICIO_SESION";
    public static final String ACCION_LOGIN_FALLIDO = "LOGIN_FALLIDO";
    public static final String ACCION_LOGIN_BLOQUEADO = "LOGIN_BLOQUEADO";
    public static final String ACCION_CIERRE_SESION = "CIERRE_SESION";

    public static final String MENSAJE_GENERICO = "Usuario o clave incorrectos.";

    private final UsuarioRepository usuarios;
    private final AuditoriaService auditoria;
    private final RegistroAuditoriaRepository registros;
    private final int maxIntentos;
    private final int minutosBloqueo;

    public AutenticacionService(UsuarioRepository usuarios, AuditoriaService auditoria,
                                RegistroAuditoriaRepository registros,
                                @Value("${hce.login.max-intentos:5}") int maxIntentos,
                                @Value("${hce.login.minutos-bloqueo:15}") int minutosBloqueo) {
        this.usuarios = usuarios;
        this.auditoria = auditoria;
        this.registros = registros;
        this.maxIntentos = maxIntentos;
        this.minutosBloqueo = minutosBloqueo;
    }

    /** Version sin IP (la usan los tests). Equivale a iniciarSesion(usuario, clave, null). */
    @Transactional(noRollbackFor = NoAutenticadoException.class)
    public Usuario iniciarSesion(String nombreUsuario, String clave) {
        return iniciarSesion(nombreUsuario, clave, null);
    }

    /**
     * Inicio de sesion con auditoria (RN-07) y bloqueo (RN-12). noRollbackFor: el rechazo lanza excepcion pero el
     * registro del intento SI se guarda. Un usuario que no existe no se puede registrar (la auditoria exige un usuario).
     * El mensaje de clave o usuario incorrecto es el mismo en los dos casos; el de cuenta bloqueada solo aparece
     * para cuentas que existen (se acepta, es lo habitual en un bloqueo por intentos).
     */
    @Transactional(noRollbackFor = NoAutenticadoException.class)
    public Usuario iniciarSesion(String nombreUsuario, String clave, String ip) {
        Usuario u = usuarios.findByUsuario(nombreUsuario)
            .orElseThrow(() -> new NoAutenticadoException(MENSAJE_GENERICO));

        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime desbloqueo = desbloqueoDe(u, ahora);
        if (desbloqueo != null) {
            // Un intento durante el bloqueo se registra, pero NO cuenta como fallido (no alarga el bloqueo).
            auditoria.registrar(u, ACCION_LOGIN_BLOQUEADO, "usuario", u.getId(), null, ip);
            long minutos = Math.max(1, (Duration.between(ahora, desbloqueo).getSeconds() + 59) / 60);
            throw new CuentaBloqueadaException("La cuenta está bloqueada por demasiados intentos fallidos. "
                + "Intenta de nuevo en " + minutos + (minutos == 1 ? " minuto." : " minutos."));
        }

        if (!"ACTIVO".equals(u.getEstado()) || !ClaveUtil.coincide(clave, u.getContrasenaHash())) {
            auditoria.registrar(u, ACCION_LOGIN_FALLIDO, "usuario", u.getId(), null, ip);
            throw new NoAutenticadoException(MENSAJE_GENERICO);
        }

        if (ClaveUtil.necesitaMigrar(u.getContrasenaHash())) {
            u.actualizarContrasenaHash(ClaveUtil.bcrypt(clave)); // RN-12: migra del SHA-256 de la demo a BCrypt
        }
        u.setUltimoAcceso(ahora);
        auditoria.registrar(u, ACCION_INICIO_SESION, "usuario", u.getId(), null, ip);
        return u;
    }

    /** Cierre de sesion auditado (RN-07). */
    @Transactional
    public void cerrarSesion(Usuario usuario, String ip) {
        auditoria.registrar(usuario, ACCION_CIERRE_SESION, "usuario", usuario.getId(), null, ip);
    }

    /**
     * Si la cuenta esta bloqueada devuelve cuando se levanta; si no, null.
     * Bloqueada = hay al menos maxIntentos LOGIN_FALLIDO posteriores al ultimo INICIO_SESION y dentro de la ventana
     * de minutosBloqueo. Se levanta cuando el mas antiguo de esos intentos sale de la ventana.
     */
    private LocalDateTime desbloqueoDe(Usuario u, LocalDateTime ahora) {
        int ultimoExito = registros.findFirstByUsuarioIdAndAccionOrderByIdDesc(u.getId(), ACCION_INICIO_SESION)
            .map(RegistroAuditoria::getId).orElse(0);
        List<RegistroAuditoria> fallidos = registros.fallidosRecientes(u.getId(), ACCION_LOGIN_FALLIDO, ultimoExito,
            ahora.minusMinutes(minutosBloqueo));
        if (fallidos.size() < maxIntentos) {
            return null;
        }
        // fallidos viene del mas reciente al mas antiguo: el que decide es el N-esimo mas reciente
        return fallidos.get(maxIntentos - 1).getFechaHora().plusMinutes(minutosBloqueo);
    }
}
