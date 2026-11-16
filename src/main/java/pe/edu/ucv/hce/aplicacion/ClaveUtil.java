package pe.edu.ucv.hce.aplicacion;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Hash de claves (RN-12). Las claves nuevas se guardan con BCrypt (con sal propia en cada hash).
 * El SHA-256 de la demo anterior se sigue RECONOCIENDO solo para poder migrar: cuando un usuario con hash antiguo
 * inicia sesion con su clave correcta, se vuelve a guardar con BCrypt (ver AutenticacionService).
 */
public final class ClaveUtil {

    /** Largo minimo de la politica de claves (RN-12). */
    public static final int LARGO_MINIMO = 6;

    private static final BCryptPasswordEncoder BCRYPT = new BCryptPasswordEncoder();

    private ClaveUtil() { }

    /** Hash BCrypt (empieza con "$2"). Dos hashes de la misma clave son distintos: la sal es aleatoria. */
    public static String bcrypt(String clave) {
        return BCRYPT.encode(clave);
    }

    /** Hash antiguo de la demo (SHA-256). Solo para reconocer y migrar usuarios existentes. */
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

    public static boolean esBcrypt(String hashGuardado) {
        return hashGuardado != null && hashGuardado.startsWith("$2");
    }

    /** true si el hash guardado todavia es el SHA-256 de la demo anterior y conviene migrarlo a BCrypt. */
    public static boolean necesitaMigrar(String hashGuardado) {
        return !esBcrypt(hashGuardado);
    }

    /** Comprueba la clave contra BCrypt o, si el hash es el antiguo, contra SHA-256. */
    public static boolean coincide(String claveIngresada, String hashGuardado) {
        if (claveIngresada == null || hashGuardado == null) {
            return false;
        }
        if (esBcrypt(hashGuardado)) {
            return BCRYPT.matches(claveIngresada, hashGuardado);
        }
        return MessageDigest.isEqual(
            sha256(claveIngresada).getBytes(StandardCharsets.UTF_8),
            hashGuardado.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Politica minima de claves (RN-12): al menos 6 caracteres, con una letra y un numero.
     * Devuelve el mensaje de error en español, o null si la clave es valida. Se usara al crear o cambiar claves
     * (gestion de usuarios, RF-11); hoy no hay una pantalla que las cambie.
     */
    public static String validarPolitica(String clave) {
        if (clave == null || clave.length() < LARGO_MINIMO) {
            return "La clave debe tener al menos " + LARGO_MINIMO + " caracteres.";
        }
        boolean letra = clave.chars().anyMatch(Character::isLetter);
        boolean numero = clave.chars().anyMatch(Character::isDigit);
        if (!letra || !numero) {
            return "La clave debe tener al menos una letra y un número.";
        }
        return null;
    }
}
