package pe.edu.ucv.hce.dominio;

/** RN-03: la atencion ya esta cerrada y no admite cambios (solo enmiendas). Se responde con 409. */
public class AtencionCerradaException extends RuntimeException {
    public AtencionCerradaException(String mensaje) { super(mensaje); }
}
