package pe.edu.ucv.hce.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ucv.hce.dominio.AccesoDenegadoException;
import pe.edu.ucv.hce.dominio.CodigoRol;
import pe.edu.ucv.hce.dominio.ConsentimientoAcceso;
import pe.edu.ucv.hce.dominio.HistoriaClinica;
import pe.edu.ucv.hce.dominio.Paciente;
import pe.edu.ucv.hce.dominio.RecursoNoEncontradoException;
import pe.edu.ucv.hce.dominio.Usuario;
import pe.edu.ucv.hce.persistencia.RegistroAuditoriaRepository;
import pe.edu.ucv.hce.persistencia.RolRepository;
import pe.edu.ucv.hce.persistencia.UsuarioRepository;

/**
 * CU-11 (RF-17; RN-04, RN-06, RN-07, RN-09): CP-19 y CP-20. Escenario copiado de PortalPacienteServiceTest:
 * dos pacientes con cuenta vinculada, de hospitales distintos del mismo catalogo demo.
 */
@SpringBootTest
@Transactional
class ConsentimientoServiceTest {

    private static final String IP = "127.0.0.1";

    @Autowired ConsentimientoService servicio;
    @Autowired RegistroPacienteService registro;
    @Autowired UsuarioRepository usuarios;
    @Autowired RolRepository roles;
    @Autowired RegistroAuditoriaRepository auditoria;

    Usuario medicoA;
    Usuario admisionA;
    Usuario pacienteX;
    Usuario pacienteY;
    HistoriaClinica historiaX;

    @BeforeEach
    void escenario() {
        admisionA = usuarios.findByUsuario("admision.a").orElseThrow();
        medicoA = usuarios.findByUsuario("medico.a").orElseThrow();
        historiaX = registro.registrarPacienteYAbrirHistoria(paciente("70000031", "Equis"), admisionA, IP).historia();
        HistoriaClinica historiaY =
            registro.registrarPacienteYAbrirHistoria(paciente("70000032", "Ye"), admisionA, IP).historia();
        pacienteX = cuenta(historiaX.getPaciente(), "paciente.consent.x");
        pacienteY = cuenta(historiaY.getPaciente(), "paciente.consent.y");
    }

    private Paciente paciente(String dni, String apellido) {
        return new Paciente("DNI", dni, "Paciente de prueba", apellido, LocalDate.of(1990, 1, 1), "F",
            "999111222", "prueba@example.com", "Calle Falsa 123");
    }

    private Usuario cuenta(Paciente p, String nombre) {
        Usuario u = usuarios.save(new Usuario(null, roles.findByNombre(CodigoRol.PACIENTE.name()).orElseThrow(),
            nombre, "hash", "ACTIVO"));
        p.vincularUsuario(u);
        return u;
    }

    private ConsentimientoAcceso otorgar(Usuario u) {
        return servicio.otorgar(u, "Hospital Demo B", "Lectura de mi historial para una interconsulta", IP);
    }

    private boolean auditado(String accion, Usuario u) {
        return auditoria.findAll().stream().anyMatch(r -> accion.equals(r.getAccion())
            && u.getId().equals(r.getUsuario().getId()) && IP.equals(r.getIp()));
    }

    /** CP-19: otorgar crea un consentimiento ACTIVO con fecha, quien lo recibe y el alcance. */
    @Test
    void otorgarRegistraFechaYAlcance() {
        ConsentimientoAcceso c = otorgar(pacienteX);
        assertEquals(ConsentimientoAcceso.ACTIVO, c.getEstado());
        assertTrue(c.estaActivo());
        assertEquals("Hospital Demo B", c.getOtorgadoA());
        assertEquals("Lectura de mi historial para una interconsulta", c.getAlcance());
        assertNotNull(c.getFechaOtorgado());
        assertNull(c.getFechaRevocado());
        assertEquals(1, servicio.listar(pacienteX, IP).size());
    }

    /** CP-20: revocar pasa a REVOCADO, guarda la fecha y el consentimiento ya no figura activo. */
    @Test
    void revocarCortaElAcceso() {
        ConsentimientoAcceso c = otorgar(pacienteX);
        ConsentimientoAcceso revocado = servicio.revocar(pacienteX, c.getId(), IP);
        assertEquals(ConsentimientoAcceso.REVOCADO, revocado.getEstado());
        assertFalse(revocado.estaActivo());
        assertNotNull(revocado.getFechaRevocado());
        assertFalse(servicio.listar(pacienteX, IP).get(0).estaActivo());
    }

    /** Revocar dos veces el mismo consentimiento se rechaza (IllegalArgumentException) y no pisa la fecha. */
    @Test
    void noSeRevocaDosVeces() {
        ConsentimientoAcceso c = otorgar(pacienteX);
        var primera = servicio.revocar(pacienteX, c.getId(), IP).getFechaRevocado();
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
            () -> servicio.revocar(pacienteX, c.getId(), IP));
        assertEquals("Este consentimiento ya fue revocado.", e.getMessage());
        assertEquals(primera, servicio.listar(pacienteX, IP).get(0).getFechaRevocado());
    }

    /** RN-04: el paciente A no ve ni revoca los consentimientos del paciente B (404 al revocar). */
    @Test
    void unPacienteNoToca_losConsentimientosDeOtro() {
        ConsentimientoAcceso deX = otorgar(pacienteX);
        assertTrue(servicio.listar(pacienteY, IP).isEmpty());
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.revocar(pacienteY, deX.getId(), IP));
        assertTrue(servicio.listar(pacienteX, IP).get(0).estaActivo());
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.revocar(pacienteX, 999999, IP));
    }

    /** RN-02/DEC-03: solo el rol PACIENTE gestiona consentimientos; medico o admision: AccesoDenegadoException. */
    @Test
    void soloElPacienteGestionaConsentimientos() {
        assertThrows(AccesoDenegadoException.class, () -> servicio.listar(medicoA, IP));
        assertThrows(AccesoDenegadoException.class, () -> servicio.otorgar(admisionA, "Hospital B", "Lectura", IP));
        assertThrows(AccesoDenegadoException.class, () -> servicio.revocar(medicoA, 1, IP));
        Usuario suelta = usuarios.save(new Usuario(null, roles.findByNombre(CodigoRol.PACIENTE.name()).orElseThrow(),
            "paciente.consent.suelta", "hash", "ACTIVO"));
        assertThrows(AccesoDenegadoException.class, () -> servicio.listar(suelta, IP));
    }

    /** Campos vacios (otorgadoA, alcance) se rechazan con mensaje en español. */
    @Test
    void rechazaCamposVacios() {
        IllegalArgumentException a = assertThrows(IllegalArgumentException.class,
            () -> servicio.otorgar(pacienteX, " ", "Lectura", IP));
        assertEquals("Indica a quién das el acceso.", a.getMessage());
        IllegalArgumentException b = assertThrows(IllegalArgumentException.class,
            () -> servicio.otorgar(pacienteX, "Hospital Demo B", "", IP));
        assertEquals("Indica el alcance del acceso.", b.getMessage());
        assertTrue(servicio.listar(pacienteX, IP).isEmpty());
    }

    /** RN-07: otorgar y revocar quedan auditados con usuario e IP. */
    @Test
    void lasAccionesQuedanAuditadas() {
        ConsentimientoAcceso c = otorgar(pacienteX);
        assertTrue(auditado(ConsentimientoService.ACCION_OTORGAR, pacienteX));
        servicio.revocar(pacienteX, c.getId(), IP);
        assertTrue(auditado(ConsentimientoService.ACCION_REVOCAR, pacienteX));
        List<ConsentimientoAcceso> lista = servicio.listar(pacienteX, IP);
        assertEquals(1, lista.size());
        assertTrue(auditado(ConsentimientoService.ACCION_LISTAR, pacienteX));
    }
}
