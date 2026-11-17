package pe.edu.ucv.hce.dominio;

/** RN-13: cambio de estado de una admision fuera del orden permitido. Responde 409. */
public class TransicionInvalidaException extends RuntimeException {
    public TransicionInvalidaException(String mensaje) {
        super(mensaje);
    }
}
