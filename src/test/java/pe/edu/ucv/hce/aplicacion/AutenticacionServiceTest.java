package pe.edu.ucv.hce.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}
