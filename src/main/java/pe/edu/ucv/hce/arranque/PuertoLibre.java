package pe.edu.ucv.hce.arranque;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

/**
 * Busca un puerto libre para la demo portable (CLAUDE.md, "Puerto"): se prueba el 8090 y, si esta ocupado,
 * el siguiente, hasta un maximo de intentos. Asi la demo no choca con otro proyecto de Spring en la misma PC.
 */
public final class PuertoLibre {

    private PuertoLibre() { }

    /** Primer puerto libre en [inicial, inicial + intentos). Si ninguno esta libre devuelve el inicial. */
    public static int desde(int inicial, int intentos) {
        for (int puerto = inicial; puerto < inicial + intentos; puerto++) {
            if (estaLibre(puerto)) {
                return puerto;
            }
        }
        return inicial;
    }

    /** Un puerto esta libre si se puede abrir en todas las interfaces, como lo hara Tomcat. */
    static boolean estaLibre(int puerto) {
        try (ServerSocket socket = new ServerSocket()) {
            socket.setReuseAddress(false);
            socket.bind(new InetSocketAddress(puerto));
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
