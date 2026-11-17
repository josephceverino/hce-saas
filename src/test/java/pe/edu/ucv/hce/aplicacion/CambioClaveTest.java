package pe.edu.ucv.hce.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ucv.hce.dominio.AccesoDenegadoException;
import pe.edu.ucv.hce.dominio.Usuario;
import pe.edu.ucv.hce.persistencia.RegistroAuditoriaRepository;
import pe.edu.ucv.hce.persistencia.UsuarioRepository;
import pe.edu.ucv.hce.presentacion.SesionActual;

/** CU-18 cambio de clave en el primer ingreso (RF-28; RN-14, RN-12, RN-07). */
@SpringBootTest
@Transactional
class CambioClaveTest {

    private static final String IP = "127.0.0.1";
    private static final String TEMPORAL = "Temporal1";

    @Autowired CuentaService cuentas;
    @Autowired UsuarioRepository usuarios;
    @Autowired RegistroAuditoriaRepository auditoria;
    @Autowired SesionActual sesionActual;

    Usuario usuario;

    @BeforeEach
    void cuentaConClaveTemporal() {
        usuario = usuarios.findByUsuario("enfermera.a").orElseThrow();
        usuario.actualizarContrasenaHash(ClaveUtil.bcrypt(TEMPORAL));
        usuario.setDebeCambiarClave(true);
        usuarios.save(usuario);
    }

    private MockHttpSession sesionDe(Usuario u) {
        MockHttpSession s = new MockHttpSession();
        s.setAttribute(SesionActual.ATRIBUTO, u.getId());
        return s;
    }

    /** RN-14: con clave temporal no se puede usar ninguna otra funcion; solo ver la sesion y cambiar la clave. */
    @Test
    void conClaveTemporalSoloSePuedeCambiarLaClave() {
        MockHttpSession s = sesionDe(usuario);
        AccesoDenegadoException e = assertThrows(AccesoDenegadoException.class, () -> sesionActual.requerir(s));
        assertEquals(SesionActual.MENSAJE_CLAVE_TEMPORAL, e.getMessage());
        assertEquals(usuario.getId(), sesionActual.requerirAunqueDebaCambiarClave(s).getId());
    }

    @Test
    void cambiarLaClaveLevantaLaObligacionYQuedaAuditado() {
        cuentas.cambiarClave(usuario, TEMPORAL, "NuevaClave9", IP);
        Usuario u = usuarios.findById(usuario.getId()).orElseThrow();
        assertFalse(u.isDebeCambiarClave());
        assertTrue(ClaveUtil.coincide("NuevaClave9", u.getContrasenaHash()));
        assertTrue(ClaveUtil.esBcrypt(u.getContrasenaHash()), "RN-12: se guarda con BCrypt");
        assertEquals(u.getId(), sesionActual.requerir(sesionDe(u)).getId(), "Ya puede usar el sistema");
        assertTrue(auditoria.findAll().stream().anyMatch(r -> CuentaService.ACCION_CAMBIO_CLAVE.equals(r.getAccion())
            && u.getId().equals(r.getUsuario().getId())));
    }

    @Test
    void rechazaLaClaveActualIncorrecta() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
            () -> cuentas.cambiarClave(usuario, "otra", "NuevaClave9", IP));
        assertEquals("La clave actual no es correcta.", e.getMessage());
        assertTrue(usuarios.findById(usuario.getId()).orElseThrow().isDebeCambiarClave());
    }

    /** RN-12: politica de claves (minimo 6 caracteres, una letra y un numero). */
    @Test
    void rechazaUnaClaveQueNoCumpleLaPolitica() {
        assertThrows(IllegalArgumentException.class, () -> cuentas.cambiarClave(usuario, TEMPORAL, "abc", IP));
        assertThrows(IllegalArgumentException.class, () -> cuentas.cambiarClave(usuario, TEMPORAL, "solotexto", IP));
    }

    @Test
    void rechazaQueLaNuevaSeaIgualALaTemporal() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
            () -> cuentas.cambiarClave(usuario, TEMPORAL, TEMPORAL, IP));
        assertEquals("La nueva clave debe ser distinta de la clave temporal.", e.getMessage());
    }
}
