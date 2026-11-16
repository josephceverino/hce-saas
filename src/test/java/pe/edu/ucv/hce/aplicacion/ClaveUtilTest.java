package pe.edu.ucv.hce.aplicacion;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Ficha M-1 (Mattias): hash de claves de la demo (CU-01; RN-12 queda para v5 con BCrypt).
 * No necesita Spring: ClaveUtil es una clase estatica.
 */
class ClaveUtilTest {

    /** CU-01: la clave demo123 coincide con su propio hash. */
    @Test
    void claveCorrectaCoincide() {
        String hash = ClaveUtil.sha256("demo123");
        assertTrue(ClaveUtil.coincide("demo123", hash));
    }

    /** CU-01: otra clave no coincide con el hash de demo123. */
    @Test
    void claveIncorrectaNoCoincide() {
        String hash = ClaveUtil.sha256("demo123");
        assertFalse(ClaveUtil.coincide("claveFalsa", hash));
    }

    /** CU-01: el hash nunca es igual a la clave en texto plano. */
    @Test
    void elHashNoEsLaClave() {
        String clave = "demo123";
        String hash = ClaveUtil.sha256(clave);
        assertNotEquals(clave, hash);
    }
}