package pe.edu.ucv.hce.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ucv.hce.dominio.AccesoDenegadoException;
import pe.edu.ucv.hce.dominio.CuentaBloqueadaException;
import pe.edu.ucv.hce.dominio.NoAutenticadoException;
import pe.edu.ucv.hce.dominio.Paciente;
import pe.edu.ucv.hce.dominio.RecursoNoEncontradoException;
import pe.edu.ucv.hce.dominio.Usuario;
import pe.edu.ucv.hce.persistencia.UsuarioRepository;

/** CU-26 Restablecer clave (RN-14, RN-16, RN-06, RN-07; D-39). */
@SpringBootTest
@Transactional
class RestablecimientoClaveServiceTest {

    private static final String IP = "127.0.0.1";

    @Autowired RestablecimientoClaveService servicio;
    @Autowired AutenticacionService autenticacion;
    @Autowired RegistroPacienteService registro;
    @Autowired UsuarioRepository usuarios;

    Usuario adminA;
    Usuario medicoA;
    Usuario medicoB;
    Usuario admisionA;
    Usuario admisionB;
    Usuario plataforma;

    @BeforeEach
    void escenario() {
        adminA = usuarios.findByUsuario("admin.a").orElseThrow();
        medicoA = usuarios.findByUsuario("medico.a").orElseThrow();
        medicoB = usuarios.findByUsuario("medico.b").orElseThrow();
        admisionA = usuarios.findByUsuario("admision.a").orElseThrow();
        admisionB = usuarios.findByUsuario("admision.b").orElseThrow();
        plataforma = usuarios.findByUsuario("plataforma").orElseThrow();
    }

    /** RN-14: la clave nueva es temporal, funciona, obliga a cambiarla y la anterior deja de servir. Queda auditado (RN-07). */
    @Test
    void elAdministradorRestableceLaClaveDeSuPersonal() {
        var r = servicio.restablecerPersonal(adminA, medicoA.getId(), IP);
        assertTrue(medicoA.isDebeCambiarClave());
        assertTrue(ClaveUtil.coincide(r.claveTemporal(), medicoA.getContrasenaHash()));
        assertFalse(medicoA.getContrasenaHash().contains(r.claveTemporal()), "Solo se guarda el hash");
        assertThrows(NoAutenticadoException.class, () -> autenticacion.iniciarSesion("medico.a", "demo123"));
        assertEquals("medico.a", autenticacion.iniciarSesion("medico.a", r.claveTemporal()).getUsuario());
    }

    /** RN-12: restablecer la clave levanta el bloqueo por intentos fallidos. */
    @Test
    void restablecerLevantaElBloqueo() {
        for (int i = 0; i < 5; i++) {
            assertThrows(NoAutenticadoException.class, () -> autenticacion.iniciarSesion("medico.a", "incorrecta1"));
        }
        assertThrows(CuentaBloqueadaException.class, () -> autenticacion.iniciarSesion("medico.a", "incorrecta1"));
        var r = servicio.restablecerPersonal(adminA, medicoA.getId(), IP);
        assertEquals("medico.a", autenticacion.iniciarSesion("medico.a", r.claveTemporal()).getUsuario());
    }

    /** RN-16, RN-06: otro hospital se trata como inexistente; el personal no restablece claves. */
    @Test
    void noRestableceClavesDeOtroHospitalNiSinPermiso() {
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.restablecerPersonal(adminA, medicoB.getId(), IP));
        assertThrows(AccesoDenegadoException.class, () -> servicio.restablecerPersonal(medicoA, medicoA.getId(), IP));
        assertThrows(AccesoDenegadoException.class, () -> servicio.restablecerPersonal(plataforma, medicoA.getId(), IP));
    }

    /** No se restablece la propia clave ni la de otros administradores por la vía del personal. */
    @Test
    void noRestableceLaPropiaClaveNiLaDeUnAdministrador() {
        assertThrows(IllegalArgumentException.class, () -> servicio.restablecerPersonal(adminA, adminA.getId(), IP));
        Usuario admin2 = usuarios.save(new Usuario(adminA.getEstablecimiento(), adminA.getRol(), "admin.a2.test", "hash", "ACTIVO"));
        assertThrows(AccesoDenegadoException.class, () -> servicio.restablecerPersonal(adminA, admin2.getId(), IP));
    }

    /** RN-16: la plataforma restablece al administrador de un hospital; nadie más. */
    @Test
    void laPlataformaRestableceAlAdministradorDelHospital() {
        Integer hospitalA = adminA.getEstablecimiento().getId();
        var r = servicio.restablecerAdministradorDeHospital(plataforma, hospitalA, IP);
        assertEquals("admin.a", r.cuenta().getUsuario());
        assertTrue(adminA.isDebeCambiarClave());
        assertThrows(AccesoDenegadoException.class, () -> servicio.restablecerAdministradorDeHospital(adminA, hospitalA, IP));
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.restablecerAdministradorDeHospital(plataforma, -1, IP));
    }

    /** RN-06, RN-15: la admisión restablece la cuenta del portal de un paciente de SU hospital. */
    @Test
    void laAdmisionRestablecePacientesSoloDeSuHospital() {
        var paciente = new Paciente("DNI", "70000191", "Paciente de prueba", "Restablece", LocalDate.of(1990, 1, 1), "F",
            "999111222", "prueba@example.com", "Calle Falsa 123");
        Integer idHistoria = registro.registrarPacienteYAbrirHistoria(paciente, admisionA, IP).historia().getId();
        var r = servicio.restablecerPaciente(admisionA, idHistoria, IP);
        assertEquals("70000191", r.cuenta().getUsuario());
        assertEquals("70000191", autenticacion.iniciarSesion("70000191", r.claveTemporal()).getUsuario());
        assertTrue(r.cuenta().isDebeCambiarClave());
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.restablecerPaciente(admisionB, idHistoria, IP));
        assertThrows(AccesoDenegadoException.class, () -> servicio.restablecerPaciente(medicoA, idHistoria, IP));
    }
}
