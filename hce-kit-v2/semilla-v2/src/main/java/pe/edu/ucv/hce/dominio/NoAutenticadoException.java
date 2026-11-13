package pe.edu.ucv.hce.dominio;

/** Se lanza cuando no hay sesion iniciada o las credenciales no son validas. */
public class NoAutenticadoException extends RuntimeException {
    public NoAutenticadoException(String mensaje) {
        super(mensaje);
    }
}
