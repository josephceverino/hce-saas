package pe.edu.ucv.hce.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ucv.hce.aplicacion.AtencionService.Ampliacion;
import pe.edu.ucv.hce.aplicacion.EnfermeriaService.DatosSignos;
import pe.edu.ucv.hce.dominio.AccesoDenegadoException;
import pe.edu.ucv.hce.dominio.Admision;
import pe.edu.ucv.hce.dominio.Diagnostico;
import pe.edu.ucv.hce.dominio.EstadoAdmision;
import pe.edu.ucv.hce.dominio.HistoriaClinica;
import pe.edu.ucv.hce.dominio.Paciente;
import pe.edu.ucv.hce.dominio.RecursoNoEncontradoException;
import pe.edu.ucv.hce.dominio.TransicionInvalidaException;
import pe.edu.ucv.hce.dominio.Usuario;
import pe.edu.ucv.hce.persistencia.UsuarioRepository;

/** CU-05 ampliado: el médico atiende desde la cola (RF-05, RF-22; RN-13, RN-03, RN-06, D-40). */
@SpringBootTest
@Transactional
class AtencionDesdeColaTest {

    private static final String IP = "127.0.0.1";
    private static final Ampliacion AMPLIADA = new Ampliacion("Dolor de 3 días", "Apetito normal", "Sin hallazgos");

    @Autowired AtencionService atenciones;
    @Autowired AdmisionService admisiones;
    @Autowired EnfermeriaService enfermeria;
    @Autowired RegistroPacienteService registro;
    @Autowired UsuarioRepository usuarios;

    Usuario admisionA;
    Usuario enfermeraA;
    Usuario medicoA;
    Usuario medicoB;
    HistoriaClinica historia;
    Admision admision;

    @BeforeEach
    void escenario() {
        admisionA = usuarios.findByUsuario("admision.a").orElseThrow();
        enfermeraA = usuarios.findByUsuario("enfermera.a").orElseThrow();
        medicoA = usuarios.findByUsuario("medico.a").orElseThrow();
        medicoB = usuarios.findByUsuario("medico.b").orElseThrow();
        historia = registro.registrarPacienteYAbrirHistoria(new Paciente("DNI", "70000101", "Paciente de prueba",
            "Cola", LocalDate.of(1980, 1, 1), "M", "999111222", "prueba@example.com", "Calle Falsa 123"),
            admisionA, IP).historia();
        admision = admisiones.admitir(admisionA, historia.getId(), Admision.CONSULTA_EXTERNA, IP);
    }

    private void dejarListo() {
        admisiones.tomarEnEnfermeria(enfermeraA, admision.getId(), IP);
        enfermeria.registrarSignos(enfermeraA, admision.getId(),
            new DatosSignos(120, 80, 72, 16, new BigDecimal("36.6"), 98), IP);
        admisiones.marcarListo(enfermeraA, admision.getId(), IP);
    }

    /** RN-13: listo → en atención al iniciar; al cerrar la atención la admisión queda cerrada. Los datos ampliados se guardan. */
    @Test
    void iniciaDesdeLaColaYCierraLaAdmision() {
        dejarListo();
        var detalle = atenciones.crearDesdeAdmision(medicoA, admision.getId(), "Dolor", "Anamnesis", "Examen", AMPLIADA, IP);
        assertEquals(EstadoAdmision.EN_ATENCION, admisiones.delHospital(medicoA, admision.getId()).getEstado());
        assertEquals("Dolor de 3 días", detalle.atencion().getEnfermedadActual());
        assertEquals("Sin hallazgos", detalle.atencion().getExamenSistemas());
        assertEquals(historia.getId(), detalle.atencion().getHistoriaClinica().getId());

        Integer id = detalle.atencion().getId();
        atenciones.agregarDiagnostico(medicoA, id, "J02.9", Diagnostico.PRINCIPAL, IP);
        atenciones.cerrar(medicoA, id, IP);
        assertEquals(EstadoAdmision.ATENCION_CERRADA, admisiones.delHospital(medicoA, admision.getId()).getEstado());
    }

    /** RN-13: no se puede atender a quien aún no está listo; no queda atención creada. */
    @Test
    void noSeAtiendeSiNoEstaListo() {
        assertThrows(TransicionInvalidaException.class,
            () -> atenciones.crearDesdeAdmision(medicoA, admision.getId(), "Dolor", "A", "E", AMPLIADA, IP));
        assertEquals(EstadoAdmision.ADMITIDO, admisiones.delHospital(medicoA, admision.getId()).getEstado());
    }

    /** Una admisión genera una sola atención. */
    @Test
    void unaAdmisionNoGeneraDosAtenciones() {
        dejarListo();
        atenciones.crearDesdeAdmision(medicoA, admision.getId(), "Dolor", "A", "E", AMPLIADA, IP);
        assertThrows(IllegalArgumentException.class,
            () -> atenciones.crearDesdeAdmision(medicoA, admision.getId(), "Otra", "A", "E", AMPLIADA, IP));
    }

    /** RN-06: el médico de otro hospital no ve la admisión; la enfermera no atiende. */
    @Test
    void otroHospitalYOtrosRolesNoAtienden() {
        dejarListo();
        assertThrows(RecursoNoEncontradoException.class,
            () -> atenciones.crearDesdeAdmision(medicoB, admision.getId(), "Dolor", "A", "E", AMPLIADA, IP));
        assertThrows(AccesoDenegadoException.class,
            () -> atenciones.crearDesdeAdmision(enfermeraA, admision.getId(), "Dolor", "A", "E", AMPLIADA, IP));
        assertEquals(EstadoAdmision.LISTO_PARA_ATENCION, admisiones.delHospital(medicoA, admision.getId()).getEstado());
    }

    /** Los campos ampliados son opcionales: en blanco quedan sin valor. */
    @Test
    void losCamposAmpliadosSonOpcionales() {
        dejarListo();
        var detalle = atenciones.crearDesdeAdmision(medicoA, admision.getId(), "Dolor", "A", "E",
            new Ampliacion(" ", null, ""), IP);
        assertNull(detalle.atencion().getEnfermedadActual());
        assertNull(detalle.atencion().getFuncionesBiologicas());
    }
}
