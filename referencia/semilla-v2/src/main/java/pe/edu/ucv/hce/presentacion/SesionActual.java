package pe.edu.ucv.hce.presentacion;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
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

    public Usuario requerir(HttpSession sesion) {
        Object id = sesion.getAttribute(ATRIBUTO);
        if (!(id instanceof Integer)) {
            throw new NoAutenticadoException("Inicia sesion para continuar.");
        }
        return usuarios.findById((Integer) id)
            .orElseThrow(() -> new NoAutenticadoException("Inicia sesion para continuar."));
    }
}
