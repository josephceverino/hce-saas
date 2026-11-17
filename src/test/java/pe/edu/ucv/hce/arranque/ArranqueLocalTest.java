package pe.edu.ucv.hce.arranque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/** Arranque de la demo portable: puerto libre y carpeta de datos junto al .exe (CLAUDE.md, Portabilidad). */
class ArranqueLocalTest {

    /** Si el puerto esta ocupado, se prueba el siguiente libre. */
    @Test
    void siElPuertoEstaOcupadoUsaElSiguiente() throws IOException {
        try (ServerSocket ocupado = new ServerSocket(0)) {
            int puerto = ocupado.getLocalPort();
            assertFalse(PuertoLibre.estaLibre(puerto));
            int elegido = PuertoLibre.desde(puerto, 10);
            assertTrue(elegido > puerto && elegido < puerto + 10, "debe elegir uno de los siguientes: " + elegido);
            assertTrue(PuertoLibre.estaLibre(elegido));
        }
    }

    @Test
    void siElPuertoEstaLibreUsaEse() throws IOException {
        int libre;
        try (ServerSocket temporal = new ServerSocket(0)) {
            libre = temporal.getLocalPort();
        }
        assertEquals(libre, PuertoLibre.desde(libre, 5));
    }

    /** La base H2 queda en data/hce dentro de la carpeta del programa, con barras normales (valido en Windows). */
    @Test
    void laBaseVivaJuntoAlPrograma() {
        String url = ArranqueLocal.urlH2(Path.of("C:\\demo\\HCE-Demo"));
        assertTrue(url.startsWith("jdbc:h2:file:"));
        assertTrue(url.endsWith("/data/hce"), url);
        assertFalse(url.contains("\\"), url);
    }

    /** Fuera de jpackage no hay carpeta del programa: la base sigue en ./data (desarrollo y tests). */
    @Test
    void sinJpackageNoHayCarpetaDelPrograma() {
        assertNull(System.getProperty("jpackage.app-path"));
        assertNull(ArranqueLocal.carpetaDelPrograma());
    }
}
