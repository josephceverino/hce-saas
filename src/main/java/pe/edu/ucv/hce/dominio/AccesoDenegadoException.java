package pe.edu.ucv.hce.dominio;

/** El rol del usuario no tiene permiso para la operacion (RN-02, RF-12, DEC-03). Se responde con 403. */
public class AccesoDenegadoException extends RuntimeException {
    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
