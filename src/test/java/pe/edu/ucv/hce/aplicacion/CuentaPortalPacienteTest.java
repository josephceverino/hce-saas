package pe.edu.ucv.hce.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ucv.hce.dominio.CodigoRol;
import pe.edu.ucv.hce.dominio.Paciente;
import pe.edu.ucv.hce.dominio.Usuario;
import pe.edu.ucv.hce.persistencia.PacienteRepository;
import pe.edu.ucv.hce.persistencia.UsuarioRepository;

/** CU-19 Crear la cuenta del portal del paciente al registrarlo (RF-27; RN-15, RN-14). */
@SpringBootTest
@Transactional
class CuentaPortalPacienteTest {

    private static final String IP = "127.0.0.1";

    @Autowired RegistroPacienteService registro;
    @Autowired AutenticacionService autenticacion;
    @Autowired CuentaService cuentas;
    @Autowired PortalPacienteService portal;
    @Autowired UsuarioRepository usuarios;
    @Autowired PacienteRepository pacientes;

    private Paciente paciente(String dni) {
        return new Paciente("DNI", dni, "Paciente de prueba", "Portal", LocalDate.of(1990, 1, 1), "F",
            "999111222", "prueba@example.com", "Calle Falsa 123");
    }

    /** RN-15: usuario = documento; RN-14: clave temporal; el paciente es global (sin hospital). */
    @Test
    void alRegistrarSeCreaLaCuentaDelPortal() {
        Usuario admision = usuarios.findByUsuario("admision.a").orElseThrow();
        ResultadoRegistro r = registro.registrarPacienteYAbrirHistoria(paciente("70000061"), admision, IP);
        assertEquals("70000061", r.usuarioPortal());
        assertNotNull(r.claveTemporal());
        Usuario cuenta = usuarios.findByUsuario("70000061").orElseThrow();
        assertTrue(cuenta.tieneRol(CodigoRol.PACIENTE));
        assertNull(cuenta.getEstablecimiento());
        assertTrue(cuenta.isDebeCambiarClave());
        assertEquals(cuenta.getId(), pacientes.findByUsuarioId(cuenta.getId()).orElseThrow().getUsuario().getId());
        assertFalse(cuenta.getContrasenaHash().contains(r.claveTemporal()), "Solo se guarda el hash");
    }

    /** RN-15: una sola cuenta por paciente. Si vuelve a admision (mismo hospital), no se crea otra. */
    @Test
    void noSeCreaUnaSegundaCuenta() {
        Usuario admision = usuarios.findByUsuario("admision.a").orElseThrow();
        registro.registrarPacienteYAbrirHistoria(paciente("70000062"), admision, IP);
        ResultadoRegistro otraVez = registro.registrarPacienteYAbrirHistoria(paciente("70000062"), admision, IP);
        assertTrue(otraVez.yaExistia());
        assertNull(otraVez.claveTemporal());
        assertEquals(1, usuarios.findAll().stream().filter(u -> u.getUsuario().equals("70000062")).count());
    }

    /** Paso 9 del recorrido: el paciente entra con la temporal, la cambia (CU-18) y ve su historial (CU-10). */
    @Test
    void elPacienteEntraCambiaSuClaveYVeSuHistoria() {
        Usuario admision = usuarios.findByUsuario("admision.a").orElseThrow();
        ResultadoRegistro r = registro.registrarPacienteYAbrirHistoria(paciente("70000063"), admision, IP);
        Usuario cuenta = autenticacion.iniciarSesion("70000063", r.claveTemporal());
        assertTrue(cuenta.isDebeCambiarClave());
        cuentas.cambiarClave(cuenta, r.claveTemporal(), "MiClave2026", IP);
        assertEquals(r.historia().getId(), portal.miHistorial(cuenta, IP).historia().getId());
    }
}
