package pe.edu.ucv.hce.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import pe.edu.ucv.hce.dominio.Atencion;
import pe.edu.ucv.hce.dominio.AtencionCerradaException;
import pe.edu.ucv.hce.dominio.Cie10;
import pe.edu.ucv.hce.dominio.CodigoRol;
import pe.edu.ucv.hce.dominio.Diagnostico;
import pe.edu.ucv.hce.dominio.DiagnosticoInvalidoException;
import pe.edu.ucv.hce.dominio.HistoriaClinica;
import pe.edu.ucv.hce.dominio.Paciente;
import pe.edu.ucv.hce.dominio.RecursoNoEncontradoException;
import pe.edu.ucv.hce.dominio.Usuario;
import pe.edu.ucv.hce.persistencia.RegistroAuditoriaRepository;
import pe.edu.ucv.hce.persistencia.RolRepository;
import pe.edu.ucv.hce.persistencia.UsuarioRepository;

/**
 * CU-05 (RF-05; RN-02, RN-03, RN-06, RN-07, RN-10, RN-11). El catalogo CIE-10 (tarea M-4 de Mattias)
 * se simula con un mock: aqui se prueba la regla, no la carga del CSV.
 */
@SpringBootTest
@Transactional
class AtencionServiceTest {

    private static final String IP = "127.0.0.1";

    @Autowired AtencionService servicio;
    @Autowired RegistroPacienteService registro;
    @Autowired UsuarioRepository usuarios;
    @Autowired RolRepository roles;
    @Autowired RegistroAuditoriaRepository auditoria;
    @MockitoBean CatalogoCie10Service catalogo;

    Usuario admisionA;
    Usuario medicoA;
    Usuario medicoB;
    HistoriaClinica historiaA;

    @BeforeEach
    void escenario() {
        admisionA = usuarios.findByUsuario("admision.a").orElseThrow();
        medicoA = usuarios.findByUsuario("medico.a").orElseThrow();
        medicoB = usuarios.findByUsuario("medico.b").orElseThrow();
        historiaA = registro.registrarPacienteYAbrirHistoria(new Paciente("DNI", "70000001", "Paciente de prueba",
            "Ficticio", LocalDate.of(1990, 5, 1), "F", "999111222", "prueba@example.com", "Calle Falsa 123"),
            admisionA, IP).historia();
        when(catalogo.porCodigo("J02.9")).thenReturn(
            Optional.of(new Cie10("J02.9", "Faringitis aguda, no especificada")));
        when(catalogo.porCodigo("J00")).thenReturn(Optional.of(new Cie10("J00", "Rinofaringitis aguda")));
        when(catalogo.porCodigo("ZZZ")).thenReturn(Optional.empty());
    }

    private AtencionDetalle abrirAtencion() {
        return servicio.crear(medicoA, historiaA.getId(), "Dolor de garganta", "Hace 3 dias", "Faringe roja", IP);
    }

    private AtencionDetalle atencionCerrada() {
        Integer id = abrirAtencion().atencion().getId();
        servicio.agregarDiagnostico(medicoA, id, "J02.9", Diagnostico.PRINCIPAL, IP);
        return servicio.cerrar(medicoA, id, IP);
    }

    /** RN-11: queda ligada a la historia, al profesional del usuario y a su hospital; estado ABIERTA. */
    @Test
    void medicoCreaAtencionAbierta() {
        Atencion a = abrirAtencion().atencion();
        assertEquals(Atencion.ABIERTA, a.getEstado());
        assertFalse(a.estaCerrada());
        assertEquals(historiaA.getId(), a.getHistoriaClinica().getId());
        assertEquals(medicoA.getEstablecimiento().getId(), a.getEstablecimiento().getId());
        assertEquals(medicoA.getId(), a.getProfesional().getUsuario().getId());
    }

    /** RN-02: admision y enfermeria no crean atenciones. */
    @Test
    void soloElMedicoCreaAtenciones() {
        assertThrows(AccesoDenegadoException.class,
            () -> servicio.crear(admisionA, historiaA.getId(), "m", "a", "e", IP));
        Usuario enfermera = enfermeraDe(medicoA, "enfermera.prueba.a");
        assertThrows(AccesoDenegadoException.class,
            () -> servicio.crear(enfermera, historiaA.getId(), "m", "a", "e", IP));
    }

    /** RN-06: el medico del hospital B no atiende en una historia del hospital A. */
    @Test
    void medicoDeOtroHospitalNoPuedeAtender() {
        assertThrows(AccesoDenegadoException.class,
            () -> servicio.crear(medicoB, historiaA.getId(), "m", "a", "e", IP));
    }

    @Test
    void historiaInexistenteDa404() {
        assertThrows(RecursoNoEncontradoException.class,
            () -> servicio.crear(medicoA, 999999, "m", "a", "e", IP));
    }

    /** RN-10: un codigo fuera del catalogo se rechaza. */
    @Test
    void rechazaDiagnosticoFueraDelCatalogo() {
        Integer id = abrirAtencion().atencion().getId();
        assertThrows(DiagnosticoInvalidoException.class,
            () -> servicio.agregarDiagnostico(medicoA, id, "ZZZ", Diagnostico.PRINCIPAL, IP));
    }

    /** CU-05: varios diagnosticos; la descripcion sale del catalogo. */
    @Test
    void admiteVariosDiagnosticos() {
        Integer id = abrirAtencion().atencion().getId();
        Diagnostico principal = servicio.agregarDiagnostico(medicoA, id, "J02.9", Diagnostico.PRINCIPAL, IP);
        servicio.agregarDiagnostico(medicoA, id, "J00", Diagnostico.SECUNDARIO, IP);
        assertEquals("Faringitis aguda, no especificada", principal.getDescripcion());
        assertEquals(2, servicio.detalle(medicoA, id, IP).diagnosticos().size());
    }

    @Test
    void rechazaTipoDeDiagnosticoDesconocido() {
        Integer id = abrirAtencion().atencion().getId();
        assertThrows(IllegalArgumentException.class,
            () -> servicio.agregarDiagnostico(medicoA, id, "J02.9", "OTRO", IP));
    }

    /** CU-05: no se cierra sin diagnostico. */
    @Test
    void noCierraSinDiagnostico() {
        Integer id = abrirAtencion().atencion().getId();
        assertThrows(IllegalArgumentException.class, () -> servicio.cerrar(medicoA, id, IP));
    }

    /** RN-03: cerrada es inmutable: no admite diagnosticos ni un segundo cierre. */
    @Test
    void atencionCerradaEsInmutable() {
        AtencionDetalle cerrada = atencionCerrada();
        Integer id = cerrada.atencion().getId();
        assertTrue(cerrada.atencion().estaCerrada());
        assertThrows(AtencionCerradaException.class,
            () -> servicio.agregarDiagnostico(medicoA, id, "J00", Diagnostico.SECUNDARIO, IP));
        assertThrows(AtencionCerradaException.class, () -> servicio.cerrar(medicoA, id, IP));
    }

    /** RN-03: la enmienda guarda autor y fecha y no cambia la atencion original. */
    @Test
    void enmiendaSobreAtencionCerrada() {
        Atencion cerrada = atencionCerrada().atencion();
        String motivoOriginal = cerrada.getMotivo();
        var enmienda = servicio.enmendar(medicoA, cerrada.getId(), "Error de dosis", "Paracetamol 500 mg c/8 h", IP);
        assertEquals("Error de dosis", enmienda.getMotivo());
        assertEquals(medicoA.getId(), enmienda.getProfesional().getUsuario().getId());
        assertTrue(enmienda.getFecha() != null);
        AtencionDetalle despues = servicio.detalle(medicoA, cerrada.getId(), IP);
        assertEquals(1, despues.enmiendas().size());
        assertEquals(motivoOriginal, despues.atencion().getMotivo());
        assertTrue(despues.atencion().estaCerrada());
    }

    /** RN-03: una atencion abierta no se enmienda. */
    @Test
    void noSeEnmiendaAtencionAbierta() {
        Integer id = abrirAtencion().atencion().getId();
        assertThrows(IllegalArgumentException.class, () -> servicio.enmendar(medicoA, id, "m", "c", IP));
    }

    /** RN-06: una atencion de otro hospital se trata como inexistente. */
    @Test
    void otroHospitalNoVeNiModificaLaAtencion() {
        Integer id = abrirAtencion().atencion().getId();
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.detalle(medicoB, id, IP));
        assertThrows(RecursoNoEncontradoException.class,
            () -> servicio.agregarDiagnostico(medicoB, id, "J02.9", Diagnostico.PRINCIPAL, IP));
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.cerrar(medicoB, id, IP));
    }

    /** RN-07: crear, diagnosticar, cerrar y enmendar quedan auditados con usuario e IP. */
    @Test
    void lasAccionesQuedanAuditadas() {
        Atencion cerrada = atencionCerrada().atencion();
        servicio.enmendar(medicoA, cerrada.getId(), "Error", "Corregido", IP);
        var acciones = auditoria.findAllByHistoriaClinicaIdOrderByIdDesc(historiaA.getId()).stream()
            .map(r -> r.getAccion()).toList();
        assertTrue(acciones.contains(AtencionService.ACCION_CREAR));
        assertTrue(acciones.contains(AtencionService.ACCION_DIAGNOSTICO));
        assertTrue(acciones.contains(AtencionService.ACCION_CERRAR));
        assertTrue(acciones.contains(AtencionService.ACCION_ENMENDAR));
        assertEquals(IP, auditoria.findAllByHistoriaClinicaIdOrderByIdDesc(historiaA.getId()).get(0).getIp());
    }

    private Usuario enfermeraDe(Usuario medico, String nombre) {
        return usuarios.save(new Usuario(medico.getEstablecimiento(),
            roles.findByNombre(CodigoRol.ENFERMERIA.name()).orElseThrow(), nombre, "hash", "ACTIVO"));
    }
}
