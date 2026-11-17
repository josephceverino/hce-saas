package pe.edu.ucv.hce.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ucv.hce.dominio.AccesoDenegadoException;
import pe.edu.ucv.hce.dominio.Admision;
import pe.edu.ucv.hce.dominio.EstadoAdmision;
import pe.edu.ucv.hce.dominio.HistoriaClinica;
import pe.edu.ucv.hce.dominio.Paciente;
import pe.edu.ucv.hce.dominio.RecursoNoEncontradoException;
import pe.edu.ucv.hce.dominio.RegistroDuplicadoException;
import pe.edu.ucv.hce.dominio.TransicionInvalidaException;
import pe.edu.ucv.hce.dominio.Usuario;
import pe.edu.ucv.hce.persistencia.RegistroAuditoriaRepository;
import pe.edu.ucv.hce.persistencia.UsuarioRepository;

/** CU-20 Admitir al paciente del dia y cola (RF-21, RF-22; RN-13, RN-06, RN-07). */
@SpringBootTest
@Transactional
class AdmisionServiceTest {

    private static final String IP = "127.0.0.1";

    @Autowired AdmisionService servicio;
    @Autowired RegistroPacienteService registro;
    @Autowired UsuarioRepository usuarios;
    @Autowired RegistroAuditoriaRepository auditoria;

    Usuario admisionA;
    Usuario admisionB;
    Usuario enfermeraA;
    Usuario medicoA;
    HistoriaClinica historiaA;
    HistoriaClinica historiaA2;

    @BeforeEach
    void escenario() {
        admisionA = usuarios.findByUsuario("admision.a").orElseThrow();
        admisionB = usuarios.findByUsuario("admision.b").orElseThrow();
        enfermeraA = usuarios.findByUsuario("enfermera.a").orElseThrow();
        medicoA = usuarios.findByUsuario("medico.a").orElseThrow();
        historiaA = registro.registrarPacienteYAbrirHistoria(paciente("70000071"), admisionA, IP).historia();
        historiaA2 = registro.registrarPacienteYAbrirHistoria(paciente("70000072"), admisionA, IP).historia();
    }

    private Paciente paciente(String dni) {
        return new Paciente("DNI", dni, "Paciente de prueba", "Admisión", LocalDate.of(1990, 1, 1), "F",
            "999111222", "prueba@example.com", "Calle Falsa 123");
    }

    /** CU-20: se admite en ADMITIDO, del hospital de la historia, y queda auditado (RN-07). */
    @Test
    void admisionAdmiteAlPaciente() {
        Admision a = servicio.admitir(admisionA, historiaA.getId(), Admision.CONSULTA_EXTERNA, IP);
        assertEquals(EstadoAdmision.ADMITIDO, a.getEstado());
        assertEquals(admisionA.getEstablecimiento().getId(), a.getEstablecimiento().getId());
        assertTrue(auditoria.findAll().stream().anyMatch(r -> AdmisionService.ACCION_ADMITIR.equals(r.getAccion())));
    }

    /** CU-20 alternativo: con una admision activa hoy, no se admite otra. */
    @Test
    void noSeAdmiteDosVecesElMismoDia() {
        servicio.admitir(admisionA, historiaA.getId(), Admision.CONSULTA_EXTERNA, IP);
        RegistroDuplicadoException e = assertThrows(RegistroDuplicadoException.class,
            () -> servicio.admitir(admisionA, historiaA.getId(), Admision.EMERGENCIA, IP));
        assertEquals("El paciente ya fue admitido hoy y sigue en atención.", e.getMessage());
    }

    /** Tras cancelar (o cerrar), el paciente puede volver a ser admitido el mismo dia. */
    @Test
    void trasCancelarSePuedeVolverAAdmitir() {
        Admision a = servicio.admitir(admisionA, historiaA.getId(), Admision.CONSULTA_EXTERNA, IP);
        servicio.cancelar(admisionA, a.getId(), IP);
        assertEquals(EstadoAdmision.ADMITIDO, servicio.admitir(admisionA, historiaA.getId(), Admision.EMERGENCIA, IP).getEstado());
    }

    /** RN-06: no se admite a un paciente de otro hospital ni se toca su admision. */
    @Test
    void otroHospitalNoAdmiteNiVeLaAdmision() {
        assertThrows(RecursoNoEncontradoException.class,
            () -> servicio.admitir(admisionB, historiaA.getId(), Admision.CONSULTA_EXTERNA, IP));
        Admision a = servicio.admitir(admisionA, historiaA.getId(), Admision.CONSULTA_EXTERNA, IP);
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.cancelar(admisionB, a.getId(), IP));
        assertTrue(servicio.cola(admisionB).isEmpty());
    }

    /** RN-02: solo admision admite; enfermeria y medico no. */
    @Test
    void soloAdmisionAdmite() {
        assertThrows(AccesoDenegadoException.class,
            () -> servicio.admitir(enfermeraA, historiaA.getId(), Admision.CONSULTA_EXTERNA, IP));
        assertThrows(AccesoDenegadoException.class,
            () -> servicio.admitir(medicoA, historiaA.getId(), Admision.CONSULTA_EXTERNA, IP));
    }

    /** RN-13 en el servicio: cada rol mueve solo su paso y en orden. */
    @Test
    void cadaRolMueveSuPasoEnOrden() {
        Admision a = servicio.admitir(admisionA, historiaA.getId(), Admision.CONSULTA_EXTERNA, IP);
        assertThrows(TransicionInvalidaException.class, () -> servicio.marcarListo(enfermeraA, a.getId(), IP),
            "No se salta EN_ENFERMERIA");
        assertThrows(AccesoDenegadoException.class, () -> servicio.tomarEnEnfermeria(medicoA, a.getId(), IP));
        servicio.tomarEnEnfermeria(enfermeraA, a.getId(), IP);
        assertThrows(TransicionInvalidaException.class, () -> servicio.cancelar(admisionA, a.getId(), IP),
            "Solo se cancela desde ADMITIDO");
        servicio.marcarListo(enfermeraA, a.getId(), IP);
        servicio.iniciarAtencion(medicoA, a.getId(), IP);
        assertEquals(EstadoAdmision.ATENCION_CERRADA, servicio.cerrarAtencion(medicoA, a.getId(), IP).getEstado());
    }

    /** RF-22: cada rol ve su parte de la cola; la emergencia va primero. */
    @Test
    void laColaDeCadaRolConLaEmergenciaPrimero() {
        Admision consulta = servicio.admitir(admisionA, historiaA.getId(), Admision.CONSULTA_EXTERNA, IP);
        Admision emergencia = servicio.admitir(admisionA, historiaA2.getId(), Admision.EMERGENCIA, IP);
        var colaEnfermeria = servicio.cola(enfermeraA);
        assertEquals(emergencia.getId(), colaEnfermeria.get(0).getId(), "La emergencia va primero aunque llegó después");
        assertEquals(consulta.getId(), colaEnfermeria.get(1).getId());
        assertTrue(servicio.cola(medicoA).isEmpty(), "El médico solo ve a los listos para atención");
        servicio.tomarEnEnfermeria(enfermeraA, consulta.getId(), IP);
        servicio.marcarListo(enfermeraA, consulta.getId(), IP);
        assertEquals(consulta.getId(), servicio.cola(medicoA).get(0).getId());
        assertEquals(2, servicio.cola(admisionA).size(), "Admisión ve todos los del día");
    }
}
