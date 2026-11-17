package pe.edu.ucv.hce.presentacion;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Rutas de las pantallas. El menu por rol (MenuService) entrega rutas de carpeta como "/pacientes/", y Spring Boot
 * solo sirve el index.html de la raiz; aqui cada carpeta de pantalla reenvia a su index.html (A-5).
 */
@Configuration
public class ConfigWeb implements WebMvcConfigurer {

    private static final String[] PANTALLAS = {
        "inicio", "pacientes", "historia", "atencion", "mi-historial", "consentimiento", "auditoria", "hospitales",
        "perfil", "login"
    };

    @Override
    public void addViewControllers(ViewControllerRegistry registro) {
        for (String pantalla : PANTALLAS) {
            registro.addViewController("/" + pantalla + "/").setViewName("forward:/" + pantalla + "/index.html");
        }
    }
}
