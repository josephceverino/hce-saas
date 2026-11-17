package pe.edu.ucv.hce.arranque;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Al terminar de arrancar abre el navegador en la URL real (puede no ser 8090 si estaba ocupado).
 * Solo se activa con hce.abrir-navegador=true, que pasa el lanzador portable; en desarrollo y en los tests no abre nada.
 * Usa rundll32 en Windows para no depender del modulo java.desktop en el runtime reducido de jpackage.
 */
@Component
@ConditionalOnProperty(name = "hce.abrir-navegador", havingValue = "true")
public class AbrirNavegador {

    private static final Logger LOG = LoggerFactory.getLogger(AbrirNavegador.class);

    private final Environment entorno;

    public AbrirNavegador(Environment entorno) {
        this.entorno = entorno;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void alIniciar() {
        String url = "http://localhost:" + entorno.getProperty("server.port", "8090") + "/";
        LOG.info("HCE-Demo lista en {} (cierra esta ventana para detener el servidor)", url);
        try {
            if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
                new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", url).start();
            } else {
                new ProcessBuilder("xdg-open", url).start();
            }
        } catch (IOException e) {
            LOG.warn("No se pudo abrir el navegador solo. Abre manualmente: {}", url);
        }
    }
}
