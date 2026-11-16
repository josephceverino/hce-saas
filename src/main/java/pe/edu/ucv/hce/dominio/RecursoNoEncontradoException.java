package pe.edu.ucv.hce.dominio;

/** El recurso pedido no existe (historia o atencion). Se responde con 404. */
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) { super(mensaje); }
}
