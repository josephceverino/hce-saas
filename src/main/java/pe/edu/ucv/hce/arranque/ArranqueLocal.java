package pe.edu.ucv.hce.arranque;

import java.nio.file.Path;

/**
 * Configuracion del arranque para la demo portable (CLAUDE.md, "Portabilidad"). Se ejecuta antes de iniciar Spring.
 * - Puerto: 8090 o el siguiente libre. Si el usuario ya fijo uno (variable SERVER_PORT, -Dserver.port o --server.port),
 *   se respeta y no se toca.
 * - Datos: cuando corre empaquetado con jpackage, la base H2 vive en la carpeta "data" JUNTO al .exe (nunca en
 *   "Archivos de programa" ni en la carpeta de trabajo). Corriendo desde Maven o el IDE queda en ./data como siempre.
 */
public final class ArranqueLocal {

    static final int PUERTO_INICIAL = 8090;
    static final int INTENTOS = 10;

    private ArranqueLocal() { }

    public static void configurar(String[] args) {
        if (!fijadoPorElUsuario("server.port", "SERVER_PORT", args)) {
            System.setProperty("server.port", String.valueOf(PuertoLibre.desde(PUERTO_INICIAL, INTENTOS)));
        }
        Path carpetaDelPrograma = carpetaDelPrograma();
        if (carpetaDelPrograma != null && !fijadoPorElUsuario("spring.datasource.url", "SPRING_DATASOURCE_URL", args)) {
            System.setProperty("spring.datasource.url", urlH2(carpetaDelPrograma));
        }
    }

    /** jpackage deja la ruta del .exe en la propiedad jpackage.app-path; sin ella no estamos empaquetados. */
    static Path carpetaDelPrograma() {
        String ruta = System.getProperty("jpackage.app-path");
        if (ruta == null || ruta.isBlank()) {
            return null;
        }
        return Path.of(ruta).toAbsolutePath().getParent();
    }

    static String urlH2(Path carpetaDelPrograma) {
        String base = carpetaDelPrograma.resolve("data").resolve("hce").toString().replace('\\', '/');
        return "jdbc:h2:file:" + base;
    }

    private static boolean fijadoPorElUsuario(String propiedad, String variable, String[] args) {
        if (System.getProperty(propiedad) != null || System.getenv(variable) != null) {
            return true;
        }
        for (String arg : args) {
            if (arg.startsWith("--" + propiedad + "=")) {
                return true;
            }
        }
        return false;
    }
}
