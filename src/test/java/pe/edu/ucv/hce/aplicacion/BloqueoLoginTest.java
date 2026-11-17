package pe.edu.ucv.hce.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ucv.hce.dominio.CuentaBloqueadaException;
import pe.edu.ucv.hce.dominio.NoAutenticadoException;
import pe.edu.ucv.hce.dominio.RegistroAuditoria;
import pe.edu.ucv.hce.dominio.Usuario;
import pe.edu.ucv.hce.persistencia.RegistroAuditoriaRepository;
import pe.edu.ucv.hce.persistencia.UsuarioRepository;

/**
 * RN-12: bloqueo tras 5 intentos fallidos consecutivos (15 minutos) y migracion de SHA-256 a BCrypt.
 * Complementa AutenticacionServiceTest (M-1). El bloqueo se calcula con la auditoria (RN-07).
 */
@SpringBootTest
@Transactional
class BloqueoLoginTest {

    private static final String IP = "127.0.0.1";

    @Autowired AutenticacionService auth;
    @Autowired UsuarioRepository usuarios;
    @Autowired RegistroAuditoriaRepository registros;

    private void fallar(String usuario, int veces) {
        for (int i = 0; i < veces; i++) {
            NoAutenticadoException e = assertThrows(NoAutenticadoException.class,
                () -> auth.iniciarSesion(usuario, "mala", IP));
            assertEquals(AutenticacionService.MENSAJE_GENERICO, e.getMessage());
        }
    }

    private long contar(Usuario u, String accion) {
        return registros.findAll().stream()
            .filter(r -> r.getUsuario().getId().equals(u.getId()) && r.getAccion().equals(accion)).count();
    }

    /** RN-12: 5 intentos fallidos bloquean la cuenta; entonces ni la clave correcta entra. */
    @Test
    void cincoIntentosFallidosBloqueanLaCuenta() {
        fallar("medico.a", 5);
        CuentaBloqueadaException e = assertThrows(CuentaBloqueadaException.class,
            () -> auth.iniciarSesion("medico.a", "demo123", IP));
        assertTrue(e.getMessage().contains("bloqueada"), e.getMessage());
        assertTrue(e.getMessage().contains("15 minutos"), e.getMessage());
    }

    /** Cuatro fallos no bloquean: la clave correcta entra. */
    @Test
    void cuatroIntentosNoBloquean() {
        fallar("medico.a", 4);
        assertEquals("medico.a", auth.iniciarSesion("medico.a", "demo123", IP).getUsuario());
    }

    /** Un inicio de sesion correcto reinicia la cuenta: los intentos son CONSECUTIVOS. */
    @Test
    void unExitoReiniciaElConteo() {
        fallar("medico.a", 4);
        auth.iniciarSesion("medico.a", "demo123", IP);
        fallar("medico.a", 4);
        assertEquals("medico.a", auth.iniciarSesion("medico.a", "demo123", IP).getUsuario());
    }

    /** El bloqueo es por usuario: bloquear a medico.a no afecta a medico.b. */
    @Test
    void elBloqueoEsPorUsuario() {
        fallar("medico.a", 5);
        assertThrows(CuentaBloqueadaException.class, () -> auth.iniciarSesion("medico.a", "demo123", IP));
        assertEquals("medico.b", auth.iniciarSesion("medico.b", "demo123", IP).getUsuario());
    }

    /** El bloqueo se levanta solo pasado el tiempo: 5 intentos de hace 20 minutos ya no cuentan. */
    @Test
    void elBloqueoSeLevantaPasadoElTiempo() {
        Usuario u = usuarios.findByUsuario("medico.a").orElseThrow();
        for (int i = 0; i < 5; i++) {
            registros.save(new RegistroAuditoria(u, null, LocalDateTime.now().minusMinutes(20),
                AutenticacionService.ACCION_LOGIN_FALLIDO, "usuario", u.getId(), IP));
        }
        assertEquals("medico.a", auth.iniciarSesion("medico.a", "demo123", IP).getUsuario());
    }

    /** Ventana deslizante: 3 intentos viejos y 4 recientes son 4 dentro de la ventana, asi que aun no bloquea. */
    @Test
    void soloCuentanLosIntentosDeLaVentana() {
        Usuario u = usuarios.findByUsuario("medico.a").orElseThrow();
        for (int i = 0; i < 3; i++) {
            registros.save(new RegistroAuditoria(u, null, LocalDateTime.now().minusMinutes(30),
                AutenticacionService.ACCION_LOGIN_FALLIDO, "usuario", u.getId(), IP));
        }
        fallar("medico.a", 4);
        assertEquals("medico.a", auth.iniciarSesion("medico.a", "demo123", IP).getUsuario());
    }

    /** RN-07: un intento durante el bloqueo queda registrado, pero NO alarga el bloqueo. */
    @Test
    void elIntentoBloqueadoSeAudita() {
        Usuario u = usuarios.findByUsuario("medico.a").orElseThrow();
        fallar("medico.a", 5);
        for (int i = 0; i < 3; i++) {
            assertThrows(CuentaBloqueadaException.class, () -> auth.iniciarSesion("medico.a", "mala", IP));
        }
        assertEquals(3, contar(u, AutenticacionService.ACCION_LOGIN_BLOQUEADO));
        assertEquals(5, contar(u, AutenticacionService.ACCION_LOGIN_FALLIDO));
    }

    /** Un usuario que no existe recibe el mismo mensaje generico (no se revela si la cuenta existe). */
    @Test
    void usuarioInexistenteMensajeGenerico() {
        NoAutenticadoException e = assertThrows(NoAutenticadoException.class,
            () -> auth.iniciarSesion("nadie", "x", IP));
        assertEquals(AutenticacionService.MENSAJE_GENERICO, e.getMessage());
    }

    /** RN-12: los usuarios demo ya estan guardados con BCrypt. */
    @Test
    void losUsuariosDemoEstanConBcrypt() {
        for (String nombre : new String[] {"admision.a", "medico.a", "enfermera.b", "admin.a", "plataforma"}) {
            assertTrue(ClaveUtil.esBcrypt(usuarios.findByUsuario(nombre).orElseThrow().getContrasenaHash()), nombre);
        }
    }

    /** RN-12: un usuario con hash antiguo (SHA-256) migra a BCrypt al entrar con su clave correcta. */
    @Test
    void elHashAntiguoSeMigraAlEntrar() {
        Usuario base = usuarios.findByUsuario("medico.a").orElseThrow();
        Usuario antiguo = usuarios.save(new Usuario(base.getEstablecimiento(), base.getRol(), "usuario.antiguo",
            ClaveUtil.sha256("abc123"), "ACTIVO"));
        // con clave incorrecta no se migra
        assertThrows(NoAutenticadoException.class, () -> auth.iniciarSesion("usuario.antiguo", "otra", IP));
        assertFalse(ClaveUtil.esBcrypt(antiguo.getContrasenaHash()));
        // con la correcta si, y despues sigue entrando
        auth.iniciarSesion("usuario.antiguo", "abc123", IP);
        assertTrue(ClaveUtil.esBcrypt(antiguo.getContrasenaHash()));
        assertEquals("usuario.antiguo", auth.iniciarSesion("usuario.antiguo", "abc123", IP).getUsuario());
    }

    /** Un usuario inactivo no entra aunque la clave sea correcta. */
    @Test
    void usuarioInactivoNoEntra() {
        Usuario base = usuarios.findByUsuario("medico.a").orElseThrow();
        usuarios.save(new Usuario(base.getEstablecimiento(), base.getRol(), "usuario.inactivo",
            ClaveUtil.bcrypt("abc123"), "INACTIVO"));
        assertThrows(NoAutenticadoException.class, () -> auth.iniciarSesion("usuario.inactivo", "abc123", IP));
    }
}
