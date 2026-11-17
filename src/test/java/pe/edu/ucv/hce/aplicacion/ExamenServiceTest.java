package pe.edu.ucv.hce.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ucv.hce.dominio.AccesoDenegadoException;
import pe.edu.ucv.hce.dominio.AtencionCerradaException;
import pe.edu.ucv.hce.dominio.CodigoRol;
import pe.edu.ucv.hce.dominio.Diagnostico;
import pe.edu.ucv.hce.dominio.HistoriaClinica;
import pe.edu.ucv.hce.dominio.Paciente;
import pe.edu.ucv.hce.dominio.ProfesionalSalud;
import pe.edu.ucv.hce.dominio.RecursoNoEncontradoException;
import pe.edu.ucv.hce.dominio.SolicitudExamen;
import pe.edu.ucv.hce.dominio.Usuario;
import pe.edu.ucv.hce.persistencia.ProfesionalSaludRepository;
import pe.edu.ucv.hce.persistencia.RolRepository;
import pe.edu.ucv.hce.persistencia.UsuarioRepository;

/** CU-22 Solicitar examenes y CU-23 Registrar resultado (RF-25, RF-09; RN-02, RN-03, RN-06, RN-07, RN-18). */
@SpringBootTest
@Transactional
class ExamenServiceTest {

    private static final String IP = "127.0.0.1";

    @Autowired ExamenService examenes;
    @Autowired AtencionService atenciones;
    @Autowired RegistroPacienteService registro;
    @Autowired UsuarioRepository usuarios;
    @Autowired RolRepository roles;
    @Autowired ProfesionalSaludRepository profesionales;

    Usuario admisionA;
    Usuario medicoA;
    Usuario medicoB;
    Usuario apoyoA;
    Usuario apoyoB;
    Integer idAtencion;
    HistoriaClinica historia;

    @BeforeEach
    void escenario() {
        admisionA = usuarios.findByUsuario("admision.a").orElseThrow();
        medicoA = usuarios.findByUsuario("medico.a").orElseThrow();
        medicoB = usuarios.findByUsuario("medico.b").orElseThrow();
        apoyoA = apoyo(medicoA, "apoyo.a.test");
        apoyoB = apoyo(medicoB, "apoyo.b.test");
        historia = registro.registrarPacienteYAbrirHistoria(new Paciente("DNI", "70000111", "Paciente de prueba",
            "Examen", LocalDate.of(1985, 1, 1), "F", "999111222", "prueba@example.com", "Calle Falsa 123"),
            admisionA, IP).historia();
        idAtencion = atenciones.crear(medicoA, historia.getId(), "Fiebre", "Hace 2 dias", "Faringe roja", IP)
            .atencion().getId();
    }

    private Usuario apoyo(Usuario delHospital, String nombre) {
        Usuario u = usuarios.save(new Usuario(delHospital.getEstablecimiento(),
            roles.findByNombre(CodigoRol.APOYO_DIAGNOSTICO.name()).orElseThrow(), nombre, "hash", "ACTIVO"));
        profesionales.save(new ProfesionalSalud(u, "Apoyo", nombre, "TM-1", "Laboratorio", "APOYO_DIAGNOSTICO"));
        return u;
    }

    /** CU-22 y CU-23: el medico pide, apoyo registra y el resultado queda en la atencion (RN-18). */
    @Test
    void solicitarYRegistrarResultado() {
        SolicitudExamen s = examenes.solicitar(medicoA, idAtencion, "Hemograma completo", "En ayunas", IP);
        assertEquals(SolicitudExamen.SOLICITADO, s.getEstado());
        assertEquals(1, examenes.delHospital(apoyoA, "SOLICITADO").size());

        examenes.registrarResultado(apoyoA, s.getId(), "Hemoglobina 13 g/dL", IP);
        assertEquals(SolicitudExamen.RESULTADO_REGISTRADO, s.getEstado());
        assertEquals("Hemoglobina 13 g/dL", s.getResultado().getResultado());
        assertTrue(examenes.delHospital(apoyoA, "SOLICITADO").isEmpty());
        assertEquals(1, examenes.delHospital(apoyoA, "RESULTADO_REGISTRADO").size());
        assertEquals("Hemoglobina 13 g/dL", atenciones.detalle(medicoA, idAtencion, IP).examenes().get(0)
            .getResultado().getResultado(), "El medico ve el resultado en la atencion");
    }

    /** RN-18: un resultado solo se registra una vez. */
    @Test
    void noSeRegistraDosVecesElResultado() {
        SolicitudExamen s = examenes.solicitar(medicoA, idAtencion, "Glucosa", null, IP);
        examenes.registrarResultado(apoyoA, s.getId(), "90 mg/dL", IP);
        assertThrows(IllegalArgumentException.class, () -> examenes.registrarResultado(apoyoA, s.getId(), "100", IP));
    }

    /** RN-06, RN-18: apoyo de otro hospital no ve ni registra la solicitud. */
    @Test
    void otroHospitalNoVeNiRegistra() {
        SolicitudExamen s = examenes.solicitar(medicoA, idAtencion, "Glucosa", null, IP);
        assertTrue(examenes.delHospital(apoyoB, "SOLICITADO").isEmpty());
        assertThrows(RecursoNoEncontradoException.class, () -> examenes.registrarResultado(apoyoB, s.getId(), "x", IP));
        assertNull(s.getResultado());
    }

    /** RN-02: solo el medico pide; solo apoyo registra. */
    @Test
    void cadaRolHaceLoSuyo() {
        assertThrows(AccesoDenegadoException.class, () -> examenes.solicitar(apoyoA, idAtencion, "Glucosa", null, IP));
        SolicitudExamen s = examenes.solicitar(medicoA, idAtencion, "Glucosa", null, IP);
        assertThrows(AccesoDenegadoException.class, () -> examenes.registrarResultado(medicoA, s.getId(), "x", IP));
        assertThrows(AccesoDenegadoException.class, () -> examenes.delHospital(medicoA, "SOLICITADO"));
    }

    /** RN-03, RN-11: no se pide sobre una atencion cerrada, ajena o de otro hospital. */
    @Test
    void soloSobreUnaAtencionPropiaYAbierta() {
        assertThrows(RecursoNoEncontradoException.class, () -> examenes.solicitar(medicoB, idAtencion, "Glucosa", null, IP));
        atenciones.agregarDiagnostico(medicoA, idAtencion, "J02.9", Diagnostico.PRINCIPAL, IP);
        atenciones.cerrar(medicoA, idAtencion, IP);
        assertThrows(AtencionCerradaException.class, () -> examenes.solicitar(medicoA, idAtencion, "Glucosa", null, IP));
    }

    @Test
    void elTipoYElResultadoSonObligatorios() {
        assertThrows(IllegalArgumentException.class, () -> examenes.solicitar(medicoA, idAtencion, " ", null, IP));
        SolicitudExamen s = examenes.solicitar(medicoA, idAtencion, "Glucosa", null, IP);
        assertThrows(IllegalArgumentException.class, () -> examenes.registrarResultado(apoyoA, s.getId(), " ", IP));
    }
}
