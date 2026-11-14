package pe.edu.ucv.hce.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ucv.hce.dominio.NoAutenticadoException;
import pe.edu.ucv.hce.dominio.Usuario;

/** CU-01 (RF-10; CP-05): login de demostracion. */
@SpringBootTest
@Transactional
class AutenticacionServiceTest {

    @Autowired AutenticacionService servicio;

    @Test
    void usuarioDemoEntraConSuClave() {
        Usuario u = servicio.iniciarSesion("admision.a", "demo123");
        assertEquals("Hospital Demo A", u.getEstablecimiento().getNombre());
    }

    @Test
    void claveIncorrectaSeRechazaConMensajeGenerico() {
        NoAutenticadoException e = assertThrows(NoAutenticadoException.class,
            () -> servicio.iniciarSesion("admision.a", "mala"));
        assertEquals("Usuario o clave incorrectos.", e.getMessage());
    }

    @Test
    void usuarioInexistenteTieneElMismoMensaje() {
        NoAutenticadoException e = assertThrows(NoAutenticadoException.class,
            () -> servicio.iniciarSesion("nadie", "demo123"));
        assertEquals("Usuario o clave incorrectos.", e.getMessage());
    }

    /**
     * Ficha M-1 (Mattias), CU-01: un usuario con estado distinto de ACTIVO no entra, aunque la clave sea correcta.
     * Pista: crea un Usuario con estado "INACTIVO" usando UsuarioRepository y RolRepository.
     */
    @Test
    @Disabled("pendiente M-1")
    void usuarioInactivoNoEntra() {
    }
}
