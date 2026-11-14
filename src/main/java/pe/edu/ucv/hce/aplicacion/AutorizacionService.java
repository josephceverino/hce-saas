package pe.edu.ucv.hce.aplicacion;

import org.springframework.stereotype.Service;
import pe.edu.ucv.hce.dominio.AccesoDenegadoException;
import pe.edu.ucv.hce.dominio.CodigoRol;
import pe.edu.ucv.hce.dominio.Usuario;

/**
 * Autorizacion central por rol (DD-03, DEC-03; RN-02, RF-12).
 * Los servicios de cada caso de uso la llaman antes de actuar; los controladores y el JS no deciden permisos.
 */
@Service
public class AutorizacionService {

    public static final String MENSAJE_SIN_PERMISO = "No tienes permiso para realizar esta acción.";

    public boolean puede(Usuario usuario, Permiso permiso) {
        if (usuario == null || usuario.getRol() == null) {
            return false;
        }
        for (CodigoRol codigo : CodigoRol.values()) {
            if (usuario.tieneRol(codigo)) {
                return permiso.permite(codigo);
            }
        }
        return false; // rol sin codigo DEC-03: no tiene ningun permiso
    }

    /** Lanza AccesoDenegadoException (403) si el rol del usuario no tiene el permiso. */
    public void exigir(Usuario usuario, Permiso permiso) {
        if (!puede(usuario, permiso)) {
            throw new AccesoDenegadoException(MENSAJE_SIN_PERMISO);
        }
    }
}
