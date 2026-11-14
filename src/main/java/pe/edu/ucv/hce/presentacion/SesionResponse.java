package pe.edu.ucv.hce.presentacion;

import pe.edu.ucv.hce.dominio.Usuario;

/**
 * Datos del usuario con sesion iniciada.
 * rol: texto para mostrar ("Admisión y archivo"); rolCodigo: codigo DEC-03 ("ADMISION") para el menu por rol (A-5).
 */
public record SesionResponse(String usuario, String rol, String rolCodigo, String establecimiento) {

    public static SesionResponse de(Usuario u) {
        String hospital = u.getEstablecimiento() == null ? "Plataforma" : u.getEstablecimiento().getNombre();
        return new SesionResponse(u.getUsuario(), u.getRol().getDescripcion(), u.getRol().getNombre(), hospital);
    }
}
