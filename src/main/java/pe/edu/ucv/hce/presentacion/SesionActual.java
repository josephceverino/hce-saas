package pe.edu.ucv.hce.presentacion;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import pe.edu.ucv.hce.dominio.AccesoDenegadoException;
import pe.edu.ucv.hce.dominio.NoAutenticadoException;
import pe.edu.ucv.hce.dominio.Usuario;
import pe.edu.ucv.hce.persistencia.UsuarioRepository;

/** Obtiene el usuario de la sesion actual o rechaza la peticion. */
@Component
public class SesionActual {

    public static final String ATRIBUTO = "idUsuario";

    private final UsuarioRepository usuarios;

    public SesionActual(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    public static final String MENSAJE_CLAVE_TEMPORAL =
        "Debes cambiar tu clave temporal antes de continuar.";

    /**
     * Usuario de la sesion. RN-14 (CU-18): si debe cambiar su clave temporal, no puede usar ninguna otra funcion
     * (403); solo las que llaman a requerirAunqueDebaCambiarClave (ver sesion, cambiar clave y salir).
     */
    public Usuario requerir(HttpSession sesion) {
        Usuario u = requerirAunqueDebaCambiarClave(sesion);
        if (u.isDebeCambiarClave()) {
            throw new AccesoDenegadoException(MENSAJE_CLAVE_TEMPORAL);
        }
        return u;
    }

    public Usuario requerirAunqueDebaCambiarClave(HttpSession sesion) {
        Object id = sesion.getAttribute(ATRIBUTO);
        if (!(id instanceof Integer)) {
            throw new NoAutenticadoException("Inicia sesion para continuar.");
        }
        return usuarios.findById((Integer) id)
            .orElseThrow(() -> new NoAutenticadoException("Inicia sesion para continuar."));
    }
}
