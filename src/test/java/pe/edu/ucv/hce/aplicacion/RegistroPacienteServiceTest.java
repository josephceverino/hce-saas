package pe.edu.ucv.hce.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import pe.edu.ucv.hce.dominio.DocumentoDuplicadoException;
import pe.edu.ucv.hce.dominio.Paciente;
import pe.edu.ucv.hce.dominio.RegistroAuditoria;
import pe.edu.ucv.hce.dominio.Usuario;
import pe.edu.ucv.hce.persistencia.HistoriaClinicaRepository;
import pe.edu.ucv.hce.persistencia.PacienteRepository;
import pe.edu.ucv.hce.persistencia.RegistroAuditoriaRepository;
import pe.edu.ucv.hce.persistencia.UsuarioRepository;

/**
 * CU-03 (RF-01, RF-04; RN-01, RN-06, RN-07; DEC-02, DEC-03, DEC-06)
 * y aislamiento entre hospitales (RNF-06, CP-21 parcial).
 */
@SpringBootTest
@Transactional
class RegistroPacienteServiceTest {

    private static final String IP = "127.0.0.1";

    @Autowired RegistroPacienteService servicio;
    @Autowired UsuarioRepository usuarios;
    @Autowired PacienteRepository pacientes;
    @Autowired HistoriaClinicaRepository historias;
    @Autowired RegistroAuditoriaRepository auditoria;

    Usuario admisionA;
    Usuario admisionB;
    Usuario medicoA;

    @BeforeEach
    void usuariosDemo() {
        admisionA = usuarios.findByUsuario("admision.a").orElseThrow();
        admisionB = usuarios.findByUsuario("admision.b").orElseThrow();
        medicoA = usuarios.findByUsuario("medico.a").orElseThrow();
    }

    private Paciente paciente(String nro) {
        return new Paciente("DNI", nro, "Paciente de prueba", "Ficticio", LocalDate.of(1990, 5, 1),
            "F", "999111222", "prueba@example.com", "Calle Falsa 123");
    }

    /** CP-01 (RN-01): se crea el paciente y su historia HC-xxxxxx en el hospital del usuario. */
    @Test
    void registraPacienteYAbreHistoria() {
        ResultadoRegistro r = servicio.registrarPacienteYAbrirHistoria(paciente("12345678"), admisionA, IP);
        assertFalse(r.yaExistia());
        assertTrue(r.historia().getNroHc().matches("HC-[0-9]{6}"));
        assertEquals(admisionA.getEstablecimiento().getId(), r.historia().getEstablecimiento().getId());
    }

    /** CP-02 (RN-01, flujo alternativo de CU-03): mismo hospital, se devuelve la existente y no se crea otra. */
    @Test
    void mismoHospitalDevuelveLaHistoriaExistente() {
        ResultadoRegistro primera = servicio.registrarPacienteYAbrirHistoria(paciente("12345678"), admisionA, IP);
        long historiasAntes = historias.count();
        long pacientesAntes = pacientes.count();

        ResultadoRegistro segunda = servicio.registrarPacienteYAbrirHistoria(paciente("12345678"), admisionA, IP);

        assertTrue(segunda.yaExistia());
        assertEquals(primera.historia().getId(), segunda.historia().getId());
        assertEquals(historiasAntes, historias.count());
        assertEquals(pacientesAntes, pacientes.count());
    }

    /** CP-23 / DEC-02 (RN-01, RN-06): otro hospital es rechazado, sin datos del paciente y sin crear nada. */
    @Test
    void otroHospitalEsRechazadoSinRevelarDatos() {
        servicio.registrarPacienteYAbrirHistoria(paciente("12345678"), admisionA, IP);
        long historiasAntes = historias.count();
        long pacientesAntes = pacientes.count();

        DocumentoDuplicadoException e = assertThrows(DocumentoDuplicadoException.class,
            () -> servicio.registrarPacienteYAbrirHistoria(paciente("12345678"), admisionB, IP));

        assertEquals(RegistroPacienteService.MENSAJE_OTRO_ESTABLECIMIENTO, e.getMessage());
        assertFalse(e.getMessage().contains("12345678"));
        assertFalse(e.getMessage().contains("Ficticio"));
        assertFalse(e.getMessage().contains("HC-"));
        assertEquals(historiasAntes, historias.count());
        assertEquals(pacientesAntes, pacientes.count());
    }

    /** CP-03 (RF-01): DNI de 7 digitos o con letras no se registra. */
    @Test
    void dniInvalidoSeRechaza() {
        assertThrows(IllegalArgumentException.class,
            () -> servicio.registrarPacienteYAbrirHistoria(paciente("1234567"), admisionA, IP));
        assertThrows(IllegalArgumentException.class,
            () -> servicio.registrarPacienteYAbrirHistoria(paciente("1234567A"), admisionA, IP));
    }

    /** RNF-06 / RN-06 (CP-21 parcial): cada hospital lista solo las historias que abrio. */
    @Test
    void cadaHospitalVeSoloSusHistorias() {
        servicio.registrarPacienteYAbrirHistoria(paciente("11111111"), admisionA, IP);
        servicio.registrarPacienteYAbrirHistoria(paciente("22222222"), admisionB, IP);

        var deA = servicio.listarHistorias(admisionA);
        var deB = servicio.listarHistorias(admisionB);

        Integer idA = admisionA.getEstablecimiento().getId();
        Integer idB = admisionB.getEstablecimiento().getId();
        assertTrue(deA.stream().allMatch(h -> h.getEstablecimiento().getId().equals(idA)));
        assertTrue(deB.stream().allMatch(h -> h.getEstablecimiento().getId().equals(idB)));
        assertTrue(deA.stream().noneMatch(h -> h.getPaciente().getNroDocumento().equals("22222222")));
        assertTrue(deB.stream().noneMatch(h -> h.getPaciente().getNroDocumento().equals("11111111")));
    }

    /** DEC-03 (RF-01; RN-02): un medico no registra pacientes y no se crea nada. */
    @Test
    void medicoNoPuedeRegistrarPacientes() {
        long pacientesAntes = pacientes.count();

        AccesoDenegadoException e = assertThrows(AccesoDenegadoException.class,
            () -> servicio.registrarPacienteYAbrirHistoria(paciente("33333333"), medicoA, IP));

        assertEquals(AutorizacionService.MENSAJE_SIN_PERMISO, e.getMessage());
        assertEquals(pacientesAntes, pacientes.count());
    }

    /** RF-02 (DEC-03): el medico si puede listar las historias de su hospital. */
    @Test
    void medicoPuedeListarHistoriasDeSuHospital() {
        servicio.registrarPacienteYAbrirHistoria(paciente("44444444"), admisionA, IP);
        var lista = servicio.listarHistorias(medicoA);
        assertTrue(lista.stream().anyMatch(h -> h.getPaciente().getNroDocumento().equals("44444444")));
    }

    /** RN-07 / DEC-06 (CU-03 paso 6): registrar deja un registro de auditoria con quien, cuando, que y desde donde. */
    @Test
    void registrarDejaRegistroDeAuditoria() {
        ResultadoRegistro r = servicio.registrarPacienteYAbrirHistoria(paciente("55555555"), admisionA, IP);

        List<RegistroAuditoria> registros = auditoria.findAllByHistoriaClinicaIdOrderByIdDesc(r.historia().getId());

        assertEquals(1, registros.size());
        RegistroAuditoria reg = registros.get(0);
        assertEquals(admisionA.getId(), reg.getUsuario().getId());
        assertEquals(RegistroPacienteService.ACCION_REGISTRAR, reg.getAccion());
        assertEquals("historia_clinica", reg.getEntidadAfectada());
        assertEquals(r.historia().getId(), reg.getIdEntidadAfectada());
        assertEquals(IP, reg.getIp());
        assertTrue(reg.getFechaHora() != null);
    }

    /** RN-07: mostrar la historia existente (flujo alternativo) tambien es un acceso auditado. */
    @Test
    void verHistoriaExistenteQuedaAuditado() {
        ResultadoRegistro r = servicio.registrarPacienteYAbrirHistoria(paciente("66666666"), admisionA, IP);
        servicio.registrarPacienteYAbrirHistoria(paciente("66666666"), admisionA, IP);

        List<RegistroAuditoria> registros = auditoria.findAllByHistoriaClinicaIdOrderByIdDesc(r.historia().getId());

        assertEquals(2, registros.size());
        assertEquals(RegistroPacienteService.ACCION_VER_EXISTENTE, registros.get(0).getAccion());
    }
}
