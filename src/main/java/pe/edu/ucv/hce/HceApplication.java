package pe.edu.ucv.hce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import pe.edu.ucv.hce.arranque.ArranqueLocal;

@SpringBootApplication
public class HceApplication {
    public static void main(String[] args) {
        ArranqueLocal.configurar(args); // puerto libre y carpeta de datos junto al programa (demo portable)
        SpringApplication.run(HceApplication.class, args);
    }
}
