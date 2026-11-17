package pe.edu.ucv.hce.presentacion;

import java.time.LocalDateTime;
import java.util.List;
import pe.edu.ucv.hce.aplicacion.ItemMenu;
import pe.edu.ucv.hce.dominio.Usuario;

/**
 * Datos del usuario con sesion iniciada.
 * rol: texto para mostrar ("Admisión y archivo"); rolCodigo: codigo DEC-03 ("ADMISION").
 * menu: opciones del menu lateral para este rol, decididas por el servidor (A-5, docs/diseno_ui.md).
 * ultimoAcceso: para la pantalla "Mi perfil" (puede ser null).
 */
public record SesionResponse(String usuario, String rol, String rolCodigo, String establecimiento,
                             LocalDateTime ultimoAcceso, List<ItemMenu> menu, boolean debeCambiarClave) {

    public static SesionResponse de(Usuario u, List<ItemMenu> menu) {
        String hospital = u.getEstablecimiento() != null ? u.getEstablecimiento().getNombre()
            : u.tieneRol(pe.edu.ucv.hce.dominio.CodigoRol.PACIENTE) ? "Portal del paciente" : "Plataforma";
        return new SesionResponse(u.getUsuario(), u.getRol().getDescripcion(), u.getRol().getNombre(), hospital,
            u.getUltimoAcceso(), menu, u.isDebeCambiarClave());
    }
}
