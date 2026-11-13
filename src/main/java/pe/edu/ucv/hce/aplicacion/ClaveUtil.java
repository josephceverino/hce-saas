package pe.edu.ucv.hce.aplicacion;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Hash de claves SOLO PARA LA DEMOSTRACION (SHA-256).
 * En la version real se usara BCrypt y la politica de claves de RN-12.
 */
public final class ClaveUtil {

    private ClaveUtil() { }

    public static String sha256(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static boolean coincide(String claveIngresada, String hashGuardado) {
        return MessageDigest.isEqual(
            sha256(claveIngresada).getBytes(StandardCharsets.UTF_8),
            hashGuardado.getBytes(StandardCharsets.UTF_8));
    }
}
