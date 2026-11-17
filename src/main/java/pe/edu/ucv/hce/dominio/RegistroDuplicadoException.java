package pe.edu.ucv.hce.dominio;

/** Un dato que debe ser unico ya existe (codigo de establecimiento, nombre de usuario). Responde 409. */
public class RegistroDuplicadoException extends RuntimeException {
    public RegistroDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
