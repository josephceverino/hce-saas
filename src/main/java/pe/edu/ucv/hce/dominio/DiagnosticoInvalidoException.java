package pe.edu.ucv.hce.dominio;

/** RN-10: el codigo no pertenece al catalogo CIE-10. Se responde con 400. */
public class DiagnosticoInvalidoException extends RuntimeException {
    public DiagnosticoInvalidoException(String mensaje) { super(mensaje); }
}
