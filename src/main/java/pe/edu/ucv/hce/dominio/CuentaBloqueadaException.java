package pe.edu.ucv.hce.dominio;

/**
 * RN-12: la cuenta esta bloqueada por demasiados intentos fallidos consecutivos. Se responde con 401
 * (es una NoAutenticadoException) y un mensaje que dice cuanto falta.
 */
public class CuentaBloqueadaException extends NoAutenticadoException {
    public CuentaBloqueadaException(String mensaje) {
        super(mensaje);
    }
}
