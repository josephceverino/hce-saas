package pe.edu.ucv.hce.aplicacion;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ucv.hce.dominio.NoAutenticadoException;
import pe.edu.ucv.hce.dominio.Usuario;
import pe.edu.ucv.hce.persistencia.UsuarioRepository;

/** CU-01: iniciar sesion (version de demostracion). */
@Service
public class AutenticacionService {

    public static final String ACCION_INICIO_SESION = "INICIO_SESION";
    public static final String ACCION_LOGIN_FALLIDO = "LOGIN_FALLIDO";
    public static final String ACCION_CIERRE_SESION = "CIERRE_SESION";

    private final UsuarioRepository usuarios;
    private final AuditoriaService auditoria;

    public AutenticacionService(UsuarioRepository usuarios, AuditoriaService auditoria) {
        this.usuarios = usuarios;
        this.auditoria = auditoria;
    }

    /** Version sin IP (la usan los tests). Equivale a iniciarSesion(usuario, clave, null). */
    @Transactional(noRollbackFor = NoAutenticadoException.class)
    public Usuario iniciarSesion(String nombreUsuario, String clave) {
        return iniciarSesion(nombreUsuario, clave, null);
    }

    /**
     * CU-01 con auditoria (RN-07, RF-13): queda registro del inicio de sesion y del intento fallido de un usuario
     * que existe. noRollbackFor: el rechazo lanza excepcion pero el registro del intento SI se guarda.
     * Un usuario que no existe no se puede registrar (la auditoria exige un usuario).
     */
    @Transactional(noRollbackFor = NoAutenticadoException.class)
    public Usuario iniciarSesion(String nombreUsuario, String clave, String ip) {
        Usuario u = usuarios.findByUsuario(nombreUsuario)
            .orElseThrow(() -> new NoAutenticadoException("Usuario o clave incorrectos."));
        if (!"ACTIVO".equals(u.getEstado()) || !ClaveUtil.coincide(clave, u.getContrasenaHash())) {
            auditoria.registrar(u, ACCION_LOGIN_FALLIDO, "usuario", u.getId(), null, ip);
            throw new NoAutenticadoException("Usuario o clave incorrectos.");
        }
        u.setUltimoAcceso(LocalDateTime.now());
        auditoria.registrar(u, ACCION_INICIO_SESION, "usuario", u.getId(), null, ip);
        return u;
    }

    /** Cierre de sesion auditado (RN-07). */
    @Transactional
    public void cerrarSesion(Usuario usuario, String ip) {
        auditoria.registrar(usuario, ACCION_CIERRE_SESION, "usuario", usuario.getId(), null, ip);
    }
}
