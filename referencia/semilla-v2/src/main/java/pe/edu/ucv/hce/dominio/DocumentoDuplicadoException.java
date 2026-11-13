package pe.edu.ucv.hce.dominio;

/** Se lanza cuando ya existe un paciente con el mismo documento (RN-01). */
public class DocumentoDuplicadoException extends RuntimeException {
    public DocumentoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
