package pe.edu.ucv.hce.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** RN-12: claves con BCrypt, compatibilidad con el hash antiguo y politica minima. No necesita Spring. */
class ClaveBcryptTest {

    @Test
    void bcryptTieneSalYElHashNoEsLaClave() {
        String a = ClaveUtil.bcrypt("demo123");
        String b = ClaveUtil.bcrypt("demo123");
        assertTrue(a.startsWith("$2"), a);
        assertNotEquals(a, b, "la sal es aleatoria: dos hashes de la misma clave son distintos");
        assertNotEquals("demo123", a);
        assertTrue(ClaveUtil.coincide("demo123", a));
        assertTrue(ClaveUtil.coincide("demo123", b));
        assertFalse(ClaveUtil.coincide("otra", a));
    }

    /** El SHA-256 de la demo anterior se sigue reconociendo (para poder migrar) y se marca como "a migrar". */
    @Test
    void reconoceElHashAntiguo() {
        String antiguo = ClaveUtil.sha256("demo123");
        assertTrue(ClaveUtil.coincide("demo123", antiguo));
        assertFalse(ClaveUtil.coincide("otra", antiguo));
        assertTrue(ClaveUtil.necesitaMigrar(antiguo));
        assertFalse(ClaveUtil.necesitaMigrar(ClaveUtil.bcrypt("demo123")));
    }

    @Test
    void valoresNulosNoCoinciden() {
        assertFalse(ClaveUtil.coincide(null, ClaveUtil.bcrypt("x1y2z3")));
        assertFalse(ClaveUtil.coincide("demo123", null));
    }

    /** RN-12: politica minima: 6 caracteres, una letra y un numero. */
    @Test
    void politicaDeClaves() {
        assertNull(ClaveUtil.validarPolitica("demo123"));
        assertNull(ClaveUtil.validarPolitica("Clave2026"));
        assertEquals("La clave debe tener al menos 6 caracteres.", ClaveUtil.validarPolitica("a1"));
        assertEquals("La clave debe tener al menos 6 caracteres.", ClaveUtil.validarPolitica(null));
        assertEquals("La clave debe tener al menos una letra y un número.", ClaveUtil.validarPolitica("abcdefgh"));
        assertEquals("La clave debe tener al menos una letra y un número.", ClaveUtil.validarPolitica("12345678"));
    }
}
