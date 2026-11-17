package pe.edu.ucv.hce.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ucv.hce.aplicacion.EnfermeriaService.DatosSignos;
import pe.edu.ucv.hce.dominio.AccesoDenegadoException;
import pe.edu.ucv.hce.dominio.Admision;
import pe.edu.ucv.hce.dominio.EstadoAdmision;
import pe.edu.ucv.hce.dominio.HistoriaClinica;
import pe.edu.ucv.hce.dominio.Paciente;
import pe.edu.ucv.hce.dominio.RecursoNoEncontradoException;
import pe.edu.ucv.hce.dominio.TransicionInvalidaException;
import pe.edu.ucv.hce.dominio.Usuario;
import pe.edu.ucv.hce.persistencia.AntecedenteRepository;
import pe.edu.ucv.hce.persistencia.UsuarioRepository;

/** CU-21 Atender la cola y registrar signos vitales (RF-23, RF-07; RN-17, RN-13, RN-06). */
@SpringBootTest
@Transactional
class EnfermeriaServiceTest {

    private static final String IP = "127.0.0.1";
    private static final DatosSignos NORMALES = new DatosSignos(120, 80, 72, 16, new BigDecimal("36.6"), 98);

    @Autowired EnfermeriaService enfermeria;
    @Autowired AdmisionService admisiones;
    @Autowired RegistroPacienteService registro;
    @Autowired HistorialService historial;
    @Autowired UsuarioRepository usuarios;
    @Autowired AntecedenteRepository antecedentes;

    Usuario admisionA;
    Usuario enfermeraA;
    Usuario enfermeraB;
    Usuario medicoA;
    HistoriaClinica historia;
    Admision admision;

    @BeforeEach
    void escenario() {
        admisionA = usuarios.findByUsuario("admision.a").orElseThrow();
        enfermeraA = usuarios.findByUsuario("enfermera.a").orElseThrow();
        enfermeraB = usuarios.findByUsuario("enfermera.b").orElseThrow();
        medicoA = usuarios.findByUsuario("medico.a").orElseThrow();
        historia = registro.registrarPacienteYAbrirHistoria(new Paciente("DNI", "70000081", "Paciente de prueba",
            "Signos", LocalDate.of(1980, 1, 1), "M", "999111222", "prueba@example.com", "Calle Falsa 123"),
            admisionA, IP).historia();
        admision = admisiones.admitir(admisionA, historia.getId(), Admision.CONSULTA_EXTERNA, IP);
    }

    /** CU-21: tomar, registrar signos vitales y confirmar deja al paciente listo para el medico. */
    @Test
    void enfermeriaRegistraSignosYDejaListo() {
        admisiones.tomarEnEnfermeria(enfermeraA, admision.getId(), IP);
        var s = enfermeria.registrarSignos(enfermeraA, admision.getId(), NORMALES, IP);
        assertEquals(120, s.getPresionSistolica());
        assertEquals(EstadoAdmision.LISTO_PARA_ATENCION, admisiones.marcarListo(enfermeraA, admision.getId(), IP).getEstado());
        assertEquals(1, enfermeria.signosDeHistoria(medicoA, historia.getId()).size(), "El médico ve los signos vitales");
    }

    /** RN-17: sin signos vitales no se confirma como listo. */
    @Test
    void sinSignosVitalesNoQuedaListo() {
        admisiones.tomarEnEnfermeria(enfermeraA, admision.getId(), IP);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
            () -> admisiones.marcarListo(enfermeraA, admision.getId(), IP));
        assertEquals("Registra los signos vitales antes de dejar al paciente listo para atención.", e.getMessage());
    }

    /** RN-17: los signos se vinculan a la admision EN_ENFERMERIA; antes de tomarlo no se registran. */
    @Test
    void soloSeRegistranConElPacienteEnEnfermeria() {
        assertThrows(TransicionInvalidaException.class, () -> enfermeria.registrarSignos(enfermeraA, admision.getId(), NORMALES, IP));
    }

    /** RN-17: se rechazan los valores fuera de los rangos de demostracion (no clinicos), con mensaje claro. */
    @Test
    void rechazaValoresFueraDeRango() {
        admisiones.tomarEnEnfermeria(enfermeraA, admision.getId(), IP);
        Integer id = admision.getId();
        IllegalArgumentException temp = assertThrows(IllegalArgumentException.class, () -> enfermeria.registrarSignos(enfermeraA, id,
            new DatosSignos(120, 80, 72, 16, new BigDecimal("-2"), 98), IP));
        assertTrue(temp.getMessage().startsWith("La temperatura debe estar entre"));
        assertThrows(IllegalArgumentException.class, () -> enfermeria.registrarSignos(enfermeraA, id,
            new DatosSignos(120, 80, 72, 16, new BigDecimal("36.5"), 120), IP), "Saturación mayor que 100");
        assertThrows(IllegalArgumentException.class, () -> enfermeria.registrarSignos(enfermeraA, id,
            new DatosSignos(80, 120, 72, 16, new BigDecimal("36.5"), 98), IP), "Diastólica mayor que sistólica");
        assertThrows(IllegalArgumentException.class, () -> enfermeria.registrarSignos(enfermeraA, id,
            new DatosSignos(120, 80, 400, 16, new BigDecimal("36.5"), 98), IP), "Frecuencia cardíaca imposible");
        assertTrue(enfermeria.signosDeHistoria(enfermeraA, historia.getId()).isEmpty(), "No se guardó ninguno");
    }

    /** RN-17 y RN-06: solo enfermeria registra signos, y solo de su hospital. */
    @Test
    void soloEnfermeriaDeSuHospital() {
        admisiones.tomarEnEnfermeria(enfermeraA, admision.getId(), IP);
        assertThrows(AccesoDenegadoException.class, () -> enfermeria.registrarSignos(medicoA, admision.getId(), NORMALES, IP));
        assertThrows(RecursoNoEncontradoException.class, () -> enfermeria.registrarSignos(enfermeraB, admision.getId(), NORMALES, IP));
        assertThrows(AccesoDenegadoException.class, () -> enfermeria.signosDeHistoria(enfermeraB, historia.getId()));
    }

    /** RF-07 ampliado: antecedentes por tipo y alergias; aparecen en la historia. */
    @Test
    void antecedentesPorTipoYAlergias() {
        enfermeria.registrarAntecedente(enfermeraA, historia.getId(), "quirurgico", "Apendicectomía en 2010", IP);
        enfermeria.registrarAntecedente(medicoA, historia.getId(), "FAMILIAR", "Madre con diabetes", IP);
        enfermeria.registrarAlergia(enfermeraA, historia.getId(), "Penicilina", "Ronchas en la piel", "moderada", IP);
        assertThrows(IllegalArgumentException.class,
            () -> enfermeria.registrarAntecedente(enfermeraA, historia.getId(), "OTRO", "x", IP));
        assertThrows(AccesoDenegadoException.class,
            () -> enfermeria.registrarAntecedente(enfermeraB, historia.getId(), "PERSONAL", "x", IP));
        var h = historial.consultar(medicoA, historia.getId(), IP);
        assertEquals(2, h.antecedentes().size());
        assertTrue(h.antecedentes().stream().anyMatch(a -> "QUIRURGICO".equals(a.getTipo())));
        assertEquals("MODERADA", h.alergias().get(0).getGravedad());
    }
}
