package pe.edu.ucv.hce.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ucv.hce.dominio.AccesoDenegadoException;
import pe.edu.ucv.hce.dominio.Alergia;
import pe.edu.ucv.hce.dominio.Cie10;
import pe.edu.ucv.hce.dominio.CodigoRol;
import pe.edu.ucv.hce.dominio.Diagnostico;
import pe.edu.ucv.hce.dominio.HistoriaClinica;
import pe.edu.ucv.hce.dominio.Paciente;
import pe.edu.ucv.hce.dominio.RecursoNoEncontradoException;
import pe.edu.ucv.hce.dominio.Usuario;
import pe.edu.ucv.hce.persistencia.AlergiaRepository;
import pe.edu.ucv.hce.persistencia.PacienteRepository;
import pe.edu.ucv.hce.persistencia.RegistroAuditoriaRepository;
import pe.edu.ucv.hce.persistencia.RolRepository;
import pe.edu.ucv.hce.persistencia.UsuarioRepository;

/**
 * CU-10 portal del paciente (RF-14, RF-15, RF-16; RN-04, RN-07). El catalogo CIE-10 se simula con un mock.
 * RN-05 (representante legal) no esta implementada: DEC-05.
 */
@SpringBootTest
@Transactional
class PortalPacienteServiceTest {

    private static final String IP = "127.0.0.1";

    @Autowired PortalPacienteService portal;
    @Autowired AtencionService atenciones;
    @Autowired HistorialService historial;
    @Autowired RegistroPacienteService registro;
    @Autowired UsuarioRepository usuarios;
    @Autowired RolRepository roles;
    @Autowired AlergiaRepository alergias;
    @Autowired PacienteRepository pacientes;
    @Autowired RegistroAuditoriaRepository auditoria;
    @MockitoBean CatalogoCie10Service catalogo;

    Usuario medicoA;
    Usuario pacienteX;
    Usuario pacienteY;
    HistoriaClinica historiaX;
    HistoriaClinica historiaY;

    @BeforeEach
    void escenario() {
        Usuario admisionA = usuarios.findByUsuario("admision.a").orElseThrow();
        medicoA = usuarios.findByUsuario("medico.a").orElseThrow();
        when(catalogo.porCodigo("J02.9")).thenReturn(
            Optional.of(new Cie10("J02.9", "Faringitis aguda, no especificada")));
        when(catalogo.porCodigo("I10")).thenReturn(Optional.of(new Cie10("I10", "Hipertensión esencial (primaria)")));

        historiaX = registro.registrarPacienteYAbrirHistoria(paciente("70000021", "Equis"), admisionA, IP).historia();
        historiaY = registro.registrarPacienteYAbrirHistoria(paciente("70000022", "Ye"), admisionA, IP).historia();
        pacienteX = cuenta(historiaX.getPaciente(), "paciente.test.x");
        pacienteY = cuenta(historiaY.getPaciente(), "paciente.test.y");
    }

    private Paciente paciente(String dni, String apellido) {
        return new Paciente("DNI", dni, "Paciente de prueba", apellido, LocalDate.of(1990, 1, 1), "F",
            "999111222", "prueba@example.com", "Calle Falsa 123");
    }

    /** El paciente es global (DEC-02): su cuenta del portal no pertenece a ningun hospital. */
    private Usuario cuenta(Paciente p, String nombre) {
        Usuario u = usuarios.save(new Usuario(null, roles.findByNombre(CodigoRol.PACIENTE.name()).orElseThrow(),
            nombre, "hash", "ACTIVO"));
        p.vincularUsuario(u);
        return u;
    }

    private Integer atencion(HistoriaClinica h, String motivo, String cie, boolean cerrar) {
        Integer id = atenciones.crear(medicoA, h.getId(), motivo, "anamnesis", "examen", IP).atencion().getId();
        atenciones.agregarDiagnostico(medicoA, id, cie, Diagnostico.PRINCIPAL, IP);
        if (cerrar) {
            atenciones.cerrar(medicoA, id, IP);
        }
        return id;
    }

    /** RN-04: el paciente ve su propia historia y no la de otro paciente. */
    @Test
    void elPacienteVeSoloSuHistoria() {
        atencion(historiaX, "Dolor de garganta de X", "J02.9", true);
        atencion(historiaY, "Presion alta de Y", "I10", true);

        HistorialClinico deX = portal.miHistorial(pacienteX, IP);
        HistorialClinico deY = portal.miHistorial(pacienteY, IP);

        assertEquals(historiaX.getId(), deX.historia().getId());
        assertEquals(historiaY.getId(), deY.historia().getId());
        assertEquals("Dolor de garganta de X", deX.atenciones().get(0).atencion().getMotivo());
        assertTrue(deX.atenciones().stream().noneMatch(a -> a.atencion().getMotivo().contains("de Y")));
    }

    /** DEC-13: el paciente no ve borradores; solo atenciones cerradas. */
    @Test
    void soloVeAtencionesCerradas() {
        atencion(historiaX, "Cerrada", "J02.9", true);
        atencion(historiaX, "Borrador abierto", "I10", false);
        HistorialClinico h = portal.miHistorial(pacienteX, IP);
        assertEquals(1, h.atenciones().size());
        assertEquals("Cerrada", h.atenciones().get(0).atencion().getMotivo());
    }

    /** RN-04: solo lectura. El paciente no puede crear atenciones ni consultar historias por id. */
    @Test
    void elPacienteNoEscribeNiConsultaPorId() {
        assertThrows(AccesoDenegadoException.class,
            () -> atenciones.crear(pacienteX, historiaX.getId(), "m", "a", "e", IP));
        assertThrows(AccesoDenegadoException.class, () -> historial.consultar(pacienteX, historiaY.getId(), IP));
    }

    /** RN-04: un rol que no es paciente no usa el portal. */
    @Test
    void unMedicoNoUsaElPortal() {
        assertThrows(AccesoDenegadoException.class, () -> portal.miHistorial(medicoA, IP));
    }

    /** Una cuenta PACIENTE sin paciente vinculado se rechaza. */
    @Test
    void cuentaSinPacienteVinculado() {
        Usuario suelta = usuarios.save(new Usuario(null,
            roles.findByNombre(CodigoRol.PACIENTE.name()).orElseThrow(), "paciente.test.suelta", "hash", "ACTIVO"));
        assertThrows(AccesoDenegadoException.class, () -> portal.miHistorial(suelta, IP));
    }

    /** Un paciente con cuenta pero sin historia abierta recibe 404 con mensaje claro. */
    @Test
    void pacienteSinHistoriaDa404() {
        Paciente sinHistoria = pacientes.save(new Paciente("DNI", "70000023", "Paciente de prueba", "Sin historia",
            LocalDate.of(1990, 1, 1), "F", "999111222", "p@example.com", "Calle Falsa 123"));
        Usuario u = cuenta(sinHistoria, "paciente.test.sinhc");
        assertThrows(RecursoNoEncontradoException.class, () -> portal.miHistorial(u, IP));
    }

    /** RF-16: el resumen cuenta solo atenciones cerradas y lista diagnosticos distintos. */
    @Test
    void resumenDeLaHistoria() {
        alergias.save(new Alergia(historiaX, "Penicilina", "Urticaria", "ALTA"));
        atencion(historiaX, "Primera", "J02.9", true);
        atencion(historiaX, "Segunda", "J02.9", true);
        atencion(historiaX, "Tercera", "I10", true);
        atencion(historiaX, "Borrador", "I10", false);

        ResumenHistorial r = portal.miResumen(pacienteX, IP);

        assertEquals(3, r.totalAtenciones());
        assertEquals(2, r.diagnosticos().size()); // J02.9 e I10, sin repetir
        assertEquals(1, r.alergias().size());
        assertTrue(r.ultimaAtencion() != null && r.generadoEn() != null);
    }

    /** RN-07: ver mi historial y mi resumen quedan auditados. */
    @Test
    void laConsultaQuedaAuditada() {
        portal.miHistorial(pacienteX, IP);
        portal.miResumen(pacienteX, IP);
        var acciones = auditoria.findAllByHistoriaClinicaIdOrderByIdDesc(historiaX.getId()).stream()
            .map(r -> r.getAccion()).toList();
        assertTrue(acciones.contains(PortalPacienteService.ACCION_MI_HISTORIAL));
        assertTrue(acciones.contains(PortalPacienteService.ACCION_MI_RESUMEN));
    }
}
