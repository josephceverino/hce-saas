package pe.edu.ucv.hce.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ucv.hce.aplicacion.AtencionService.Ampliacion;
import pe.edu.ucv.hce.aplicacion.EnfermeriaService.DatosSignos;
import pe.edu.ucv.hce.dominio.Admision;
import pe.edu.ucv.hce.dominio.CodigoRol;
import pe.edu.ucv.hce.dominio.Diagnostico;
import pe.edu.ucv.hce.dominio.HistoriaClinica;
import pe.edu.ucv.hce.dominio.Paciente;
import pe.edu.ucv.hce.dominio.ProfesionalSalud;
import pe.edu.ucv.hce.dominio.Usuario;
import pe.edu.ucv.hce.persistencia.ProfesionalSaludRepository;
import pe.edu.ucv.hce.persistencia.RolRepository;
import pe.edu.ucv.hce.persistencia.UsuarioRepository;
import pe.edu.ucv.hce.presentacion.HistoriaDetalleResponse;

/**
 * R4.1: lo que ve el paciente en su portal con la historia nueva (CU-10, RF-14 a RF-16; RN-04, RN-03, RN-06).
 * Solo atenciones CERRADAS (decision del 17-nov), con datos ampliados y examenes con resultado; sin signos vitales.
 */
@SpringBootTest
@Transactional
class PortalHistoriaNuevaTest {

    private static final String IP = "127.0.0.1";

    @Autowired PortalPacienteService portal;
    @Autowired AtencionService atenciones;
    @Autowired AdmisionService admisiones;
    @Autowired EnfermeriaService enfermeria;
    @Autowired ExamenService examenes;
    @Autowired RegistroPacienteService registro;
    @Autowired UsuarioRepository usuarios;
    @Autowired RolRepository roles;
    @Autowired ProfesionalSaludRepository profesionales;

    Usuario admisionA;
    Usuario enfermeraA;
    Usuario medicoA;
    Usuario apoyoA;
    HistoriaClinica historiaX;
    HistoriaClinica historiaY;
    Usuario cuentaX;
    Usuario cuentaY;

    @BeforeEach
    void escenario() {
        admisionA = usuarios.findByUsuario("admision.a").orElseThrow();
        enfermeraA = usuarios.findByUsuario("enfermera.a").orElseThrow();
        medicoA = usuarios.findByUsuario("medico.a").orElseThrow();
        apoyoA = usuarios.save(new Usuario(medicoA.getEstablecimiento(),
            roles.findByNombre(CodigoRol.APOYO_DIAGNOSTICO.name()).orElseThrow(), "apoyo.portal.test", "hash", "ACTIVO"));
        profesionales.save(new ProfesionalSalud(apoyoA, "Apoyo", "Portal", "TM-9", "Laboratorio", "APOYO_DIAGNOSTICO"));
        historiaX = registro.registrarPacienteYAbrirHistoria(paciente("70000121", "Equis"), admisionA, IP).historia();
        historiaY = registro.registrarPacienteYAbrirHistoria(paciente("70000122", "Ye"), admisionA, IP).historia();
        cuentaX = historiaX.getPaciente().getUsuario();
        cuentaY = historiaY.getPaciente().getUsuario();
    }

    private Paciente paciente(String dni, String apellido) {
        return new Paciente("DNI", dni, "Paciente de prueba", apellido, LocalDate.of(1990, 1, 1), "F",
            "999111222", "prueba@example.com", "Calle Falsa 123");
    }

    /** Lleva a la historia por el recorrido real: admision, enfermeria con signos vitales y atencion desde la cola. */
    private Integer atencionDesdeCola(HistoriaClinica h, String motivo) {
        Admision a = admisiones.admitir(admisionA, h.getId(), Admision.CONSULTA_EXTERNA, IP);
        admisiones.tomarEnEnfermeria(enfermeraA, a.getId(), IP);
        enfermeria.registrarSignos(enfermeraA, a.getId(),
            new DatosSignos(120, 80, 72, 16, new BigDecimal("36.6"), 98), IP);
        admisiones.marcarListo(enfermeraA, a.getId(), IP);
        Integer id = atenciones.crearDesdeAdmision(medicoA, a.getId(), motivo, "anamnesis", "examen",
            new Ampliacion("Dolor de 3 días", "Apetito normal", "Sin hallazgos"), IP).atencion().getId();
        atenciones.agregarDiagnostico(medicoA, id, "J02.9", Diagnostico.PRINCIPAL, IP);
        return id;
    }

    /** El paciente ve la atención cerrada con los datos ampliados y el examen con su resultado. */
    @Test
    void veLaAtencionCerradaConDatosAmpliadosYResultado() {
        Integer id = atencionDesdeCola(historiaX, "Dolor de garganta");
        var s = examenes.solicitar(medicoA, id, "Hemograma completo", "En ayunas", IP);
        examenes.registrarResultado(apoyoA, s.getId(), "Hemoglobina 13 g/dL", IP);
        atenciones.cerrar(medicoA, id, IP);

        var dto = HistoriaDetalleResponse.de(portal.miHistorial(cuentaX, IP));
        assertEquals(1, dto.atenciones().size());
        var a = dto.atenciones().get(0);
        assertEquals("Dolor de 3 días", a.enfermedadActual());
        assertEquals("Sin hallazgos", a.examenSistemas());
        assertEquals(1, a.examenes().size());
        assertEquals("Hemograma completo", a.examenes().get(0).tipoExamen());
        assertEquals("Hemoglobina 13 g/dL", a.examenes().get(0).resultado());
    }

    /** R4.2: el resumen descargable lista solo los exámenes con resultado de atenciones cerradas. */
    @Test
    void elResumenListaLosExamenesConResultado() {
        Integer id = atencionDesdeCola(historiaX, "Dolor");
        var con = examenes.solicitar(medicoA, id, "Hemograma completo", null, IP);
        examenes.solicitar(medicoA, id, "Glucosa", null, IP); // sin resultado: no aparece
        examenes.registrarResultado(apoyoA, con.getId(), "Hemoglobina 13 g/dL", IP);
        assertTrue(portal.miResumen(cuentaX, IP).examenes().isEmpty(), "Con la atención abierta no se muestra");
        atenciones.cerrar(medicoA, id, IP);
        var resumen = pe.edu.ucv.hce.presentacion.ResumenHistorialResponse.de(portal.miResumen(cuentaX, IP));
        assertEquals(1, resumen.examenes().size());
        assertEquals("Hemograma completo", resumen.examenes().get(0).tipoExamen());
        assertEquals("Hemoglobina 13 g/dL", resumen.examenes().get(0).resultado());
        assertTrue(portal.miResumen(cuentaY, IP).examenes().isEmpty(), "RN-04");
    }

    /** Decisión del 17-nov: una atención abierta (y sus exámenes pendientes) no se muestra al paciente. */
    @Test
    void laAtencionAbiertaNoSeMuestra() {
        Integer id = atencionDesdeCola(historiaX, "En curso");
        examenes.solicitar(medicoA, id, "Glucosa", null, IP);
        assertTrue(portal.miHistorial(cuentaX, IP).atenciones().isEmpty());
        assertEquals(0, portal.miResumen(cuentaX, IP).totalAtenciones());
        atenciones.cerrar(medicoA, id, IP);
        assertEquals(1, portal.miHistorial(cuentaX, IP).atenciones().size());
        assertEquals(1, portal.miResumen(cuentaX, IP).totalAtenciones());
    }

    /** RN-04: ni la atención ni los exámenes de un paciente aparecen en la cuenta de otro. */
    @Test
    void unPacienteNoVeLosExamenesDeOtro() {
        Integer id = atencionDesdeCola(historiaX, "Dolor");
        var s = examenes.solicitar(medicoA, id, "Hemograma completo", null, IP);
        examenes.registrarResultado(apoyoA, s.getId(), "Normal", IP);
        atenciones.cerrar(medicoA, id, IP);
        assertTrue(portal.miHistorial(cuentaY, IP).atenciones().isEmpty());
        assertEquals(1, portal.miHistorial(cuentaX, IP).atenciones().size());
    }

    /** Sin signos vitales en el portal: el DTO del paciente no los expone en ningún nivel. */
    @Test
    void elPortalNoExponeSignosVitales() {
        Integer id = atencionDesdeCola(historiaX, "Dolor");
        atenciones.cerrar(medicoA, id, IP);
        String json = HistoriaDetalleResponse.de(portal.miHistorial(cuentaX, IP)).toString().toLowerCase();
        assertFalse(json.contains("signos") || json.contains("presion") || json.contains("saturacion"));
    }
}
