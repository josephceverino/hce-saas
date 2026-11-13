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
import pe.edu.ucv.hce.dominio.DocumentoDuplicadoException;
import pe.edu.ucv.hce.dominio.Establecimiento;
import pe.edu.ucv.hce.dominio.Paciente;
import pe.edu.ucv.hce.persistencia.EstablecimientoRepository;
import pe.edu.ucv.hce.persistencia.HistoriaClinicaRepository;
import pe.edu.ucv.hce.persistencia.PacienteRepository;

/** CU-03 (RF-01, RF-04; RN-01, RN-06; DEC-02) y aislamiento entre hospitales (RNF-06, CP-21 parcial). */
@SpringBootTest
@Transactional
class RegistroPacienteServiceTest {

    @Autowired RegistroPacienteService servicio;
    @Autowired EstablecimientoRepository establecimientos;
    @Autowired PacienteRepository pacientes;
    @Autowired HistoriaClinicaRepository historias;

    Establecimiento hospitalA;
    Establecimiento hospitalB;

    @BeforeEach
    void hospitales() {
        hospitalA = establecimientos.findAll().stream()
            .filter(e -> e.getNombre().equals("Hospital Demo A")).findFirst().orElseThrow();
        hospitalB = establecimientos.findAll().stream()
            .filter(e -> e.getNombre().equals("Hospital Demo B")).findFirst().orElseThrow();
    }

    private Paciente paciente(String nro) {
        return new Paciente("DNI", nro, "Paciente de prueba", "Ficticio", LocalDate.of(1990, 5, 1),
            "F", "999111222", "prueba@example.com", "Calle Falsa 123");
    }

    /** CP-01 (RN-01): se crea el paciente y su historia HC-xxxxxx. */
    @Test
    void registraPacienteYAbreHistoria() {
        ResultadoRegistro r = servicio.registrarPacienteYAbrirHistoria(paciente("12345678"), hospitalA);
        assertFalse(r.yaExistia());
        assertTrue(r.historia().getNroHc().matches("HC-[0-9]{6}"));
        assertEquals(hospitalA.getId(), r.historia().getEstablecimiento().getId());
    }

    /** CP-02 (RN-01, flujo alternativo de CU-03): mismo hospital, se devuelve la existente y no se crea otra. */
    @Test
    void mismoHospitalDevuelveLaHistoriaExistente() {
        ResultadoRegistro primera = servicio.registrarPacienteYAbrirHistoria(paciente("12345678"), hospitalA);
        long historiasAntes = historias.count();
        long pacientesAntes = pacientes.count();

        ResultadoRegistro segunda = servicio.registrarPacienteYAbrirHistoria(paciente("12345678"), hospitalA);

        assertTrue(segunda.yaExistia());
        assertEquals(primera.historia().getId(), segunda.historia().getId());
        assertEquals(historiasAntes, historias.count());
        assertEquals(pacientesAntes, pacientes.count());
    }

    /** DEC-02 (RN-01, RN-06): otro hospital es rechazado, sin datos del paciente y sin crear nada. */
    @Test
    void otroHospitalEsRechazadoSinRevelarDatos() {
        servicio.registrarPacienteYAbrirHistoria(paciente("12345678"), hospitalA);
        long historiasAntes = historias.count();
        long pacientesAntes = pacientes.count();

        DocumentoDuplicadoException e = assertThrows(DocumentoDuplicadoException.class,
            () -> servicio.registrarPacienteYAbrirHistoria(paciente("12345678"), hospitalB));

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
            () -> servicio.registrarPacienteYAbrirHistoria(paciente("1234567"), hospitalA));
        assertThrows(IllegalArgumentException.class,
            () -> servicio.registrarPacienteYAbrirHistoria(paciente("1234567A"), hospitalA));
    }

    /** RNF-06 / RN-06 (CP-21 parcial): cada hospital lista solo las historias que abrio. */
    @Test
    void cadaHospitalVeSoloSusHistorias() {
        servicio.registrarPacienteYAbrirHistoria(paciente("11111111"), hospitalA);
        servicio.registrarPacienteYAbrirHistoria(paciente("22222222"), hospitalB);

        var deA = servicio.listarHistorias(hospitalA.getId());
        var deB = servicio.listarHistorias(hospitalB.getId());

        assertTrue(deA.stream().allMatch(h -> h.getEstablecimiento().getId().equals(hospitalA.getId())));
        assertTrue(deB.stream().allMatch(h -> h.getEstablecimiento().getId().equals(hospitalB.getId())));
        assertTrue(deA.stream().noneMatch(h -> h.getPaciente().getNroDocumento().equals("22222222")));
        assertTrue(deB.stream().noneMatch(h -> h.getPaciente().getNroDocumento().equals("11111111")));
    }
}
