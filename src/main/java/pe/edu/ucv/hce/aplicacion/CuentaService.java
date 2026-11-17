package pe.edu.ucv.hce.aplicacion;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ucv.hce.dominio.Usuario;
import pe.edu.ucv.hce.persistencia.UsuarioRepository;

/**
 * CU-18 Cambiar contrasena en el primer ingreso (RF-28, RF-10; RN-14, RN-12, RN-07).
 * Sirve tambien para cualquier cambio de clave del propio usuario: exige la clave actual, la politica de RN-12
 * y que la nueva sea distinta. Al cambiarla se levanta la obligacion de RN-14.
 */
@Service
public class CuentaService {

    public static final String ACCION_CAMBIO_CLAVE = "CAMBIO_CLAVE";

    private final UsuarioRepository usuarios;
    private final AuditoriaService auditoria;

    public CuentaService(UsuarioRepository usuarios, AuditoriaService auditoria) {
        this.usuarios = usuarios;
        this.auditoria = auditoria;
    }

    @Transactional
    public Usuario cambiarClave(Usuario usuario, String claveActual, String claveNueva, String ip) {
        if (claveActual == null || !ClaveUtil.coincide(claveActual, usuario.getContrasenaHash())) {
            throw new IllegalArgumentException("La clave actual no es correcta.");
        }
        String problema = ClaveUtil.validarPolitica(claveNueva);
        if (problema != null) {
            throw new IllegalArgumentException(problema);
        }
        if (claveNueva.equals(claveActual)) {
            throw new IllegalArgumentException("La nueva clave debe ser distinta de la clave temporal.");
        }
        usuario.actualizarContrasenaHash(ClaveUtil.bcrypt(claveNueva));
        usuario.setDebeCambiarClave(false);
        Usuario guardado = usuarios.save(usuario);
        auditoria.registrar(guardado, ACCION_CAMBIO_CLAVE, "usuario", guardado.getId(), null, ip);
        return guardado;
    }
}
