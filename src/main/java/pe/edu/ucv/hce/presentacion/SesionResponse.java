package pe.edu.ucv.hce.presentacion;

import pe.edu.ucv.hce.dominio.Usuario;

/** Datos del usuario con sesion iniciada. */
public record SesionResponse(String usuario, String rol, String establecimiento) {

    public static SesionResponse de(Usuario u) {
        String hospital = u.getEstablecimiento() == null ? "Plataforma" : u.getEstablecimiento().getNombre();
        return new SesionResponse(u.getUsuario(), u.getRol().getNombre(), hospital);
    }
}
