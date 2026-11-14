package pe.edu.ucv.hce.aplicacion;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Ficha M-1 (Mattias): hash de claves de la demo (CU-01; RN-12 queda para v5 con BCrypt).
 * No necesita Spring: ClaveUtil es una clase estatica.
 */
class ClaveUtilTest {

    /** CU-01: la clave demo123 coincide con su propio hash. */
    @Test
    @Disabled("pendiente M-1")
    void claveCorrectaCoincide() {
    }

    /** CU-01: otra clave no coincide con el hash de demo123. */
    @Test
    @Disabled("pendiente M-1")
    void claveIncorrectaNoCoincide() {
    }

    /** CU-01: el hash nunca es igual a la clave en texto plano. */
    @Test
    @Disabled("pendiente M-1")
    void elHashNoEsLaClave() {
    }
}
