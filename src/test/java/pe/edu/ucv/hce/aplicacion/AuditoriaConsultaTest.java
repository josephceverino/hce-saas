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
import pe.edu.ucv.hce.dominio.CodigoRol;
import pe.edu.ucv.hce.dominio.HistoriaClinica;
import pe.edu.ucv.hce.dominio.NoAutenticadoException;
import pe.edu.ucv.hce.dominio.Paciente;
import pe.edu.ucv.hce.dominio.RegistroAuditoria;
import pe.edu.ucv.hce.dominio.Usuario;
import pe.edu.ucv.hce.persistencia.RolRepository;
import pe.edu.ucv.hce.persistencia.UsuarioRepository;

/**
 * Consulta de auditoria del Administrador del hospital (RF-13, RN-07, RNF-08) y aislamiento entre hospitales (RN-06).
 * Tambien comprueba que el login, el intento fallido y el cierre de sesion quedan registrados.
 */
@SpringBootTest
@Transactional
class AuditoriaConsultaTest {

    private static final String IP = "127.0.0.1";

    @Autowired AuditoriaService auditoria;
    @Autowired AutenticacionService autenticacion;
    @Autowired RegistroPacienteService registro;
    @Autowired HistorialService historial;
    @Autowired PortalPacienteService portal;
    @Autowired UsuarioRepository usuarios;
    @Autowired RolRepository roles;

    Usuario adminA;
    Usuario adminB;
    Usuario admisionA;
    Usuario medicoA;
    Usuario medicoB;
    HistoriaClinica historiaA;

    @BeforeEach
    void escenario() {
        adminA = usuarios.findByUsuario("admin.a").orElseThrow();
        admisionA = usuarios.findByUsuario("admision.a").orElseThrow();
        medicoA = usuarios.findByUsuario("medico.a").orElseThrow();
        medicoB = usuarios.findByUsuario("medico.b").orElseThrow();
        adminB = usuarios.save(new Usuario(medicoB.getEstablecimiento(),
            roles.findByNombre(CodigoRol.ADMIN_HOSPITAL.name()).orElseThrow(), "admin.test.b", "hash", "ACTIVO"));

        historiaA = registro.registrarPacienteYAbrirHistoria(new Paciente("DNI", "70000031", "Paciente de prueba",
            "Auditoria", LocalDate.of(1990, 1, 1), "F", "999111222", "p@example.com", "Calle Falsa 123"),
            admisionA, IP).historia();
        historial.consultar(medicoA, historiaA.getId(), IP);
        autenticacion.iniciarSesion("medico.a", "demo123", IP);
        autenticacion.iniciarSesion("medico.b", "demo123", IP);
    }

    private List<String> usuariosVistos(Usuario admin, FiltroAuditoria f) {
        return auditoria.consultar(admin, f, 0, 100).registros().stream()
            .map(r -> r.getUsuario().getUsuario()).toList();
    }

    private static final FiltroAuditoria SIN_FILTRO = new FiltroAuditoria(null, null, null, null, null);

    /** RN-06: el admin del hospital A ve lo de A y nada de B; el de B, lo contrario. */
    @Test
    void cadaAdminVeSoloLoDeSuHospital() {
        List<String> deA = usuariosVistos(adminA, SIN_FILTRO);
        assertTrue(deA.contains("admision.a") && deA.contains("medico.a"));
        assertFalse(deA.contains("medico.b"));

        List<String> deB = usuariosVistos(adminB, SIN_FILTRO);
        assertTrue(deB.contains("medico.b"));
        assertFalse(deB.contains("medico.a") || deB.contains("admision.a"));
    }

    /** RN-02/DEC-03: solo el Administrador del hospital consulta la auditoria. */
    @Test
    void soloElAdminDelHospitalConsulta() {
        assertThrows(AccesoDenegadoException.class, () -> auditoria.consultar(medicoA, SIN_FILTRO, 0, 20));
        assertThrows(AccesoDenegadoException.class, () -> auditoria.consultar(admisionA, SIN_FILTRO, 0, 20));
        assertThrows(AccesoDenegadoException.class, () -> auditoria.accionesDelHospital(medicoA));
    }

    /** El administrador de plataforma no pertenece a un hospital: no ve auditoria de hospitales. */
    @Test
    void elAdminDePlataformaNoVeAuditoriaDeHospitales() {
        Usuario plataforma = usuarios.findByUsuario("plataforma").orElseThrow();
        assertThrows(AccesoDenegadoException.class, () -> auditoria.consultar(plataforma, SIN_FILTRO, 0, 20));
    }

    @Test
    void filtraPorAccionUsuarioYHistoria() {
        var porAccion = auditoria.consultar(adminA,
            new FiltroAuditoria(null, HistorialService.ACCION_CONSULTAR, null, null, null), 0, 100).registros();
        assertFalse(porAccion.isEmpty());
        assertTrue(porAccion.stream().allMatch(r -> HistorialService.ACCION_CONSULTAR.equals(r.getAccion())));

        var porUsuario = usuariosVistos(adminA, new FiltroAuditoria("MEDICO", null, null, null, null));
        assertFalse(porUsuario.isEmpty());
        assertTrue(porUsuario.stream().allMatch(u -> u.contains("medico")));

        var porHistoria = auditoria.consultar(adminA,
            new FiltroAuditoria(null, null, null, null, historiaA.getNroHc()), 0, 100).registros();
        assertFalse(porHistoria.isEmpty());
        assertTrue(porHistoria.stream().allMatch(r -> historiaA.getNroHc().equals(r.getHistoriaClinica().getNroHc())));
    }

    @Test
    void filtraPorRangoDeFechas() {
        LocalDate hoy = LocalDate.now();
        assertFalse(auditoria.consultar(adminA, new FiltroAuditoria(null, null, hoy, hoy, null), 0, 100)
            .registros().isEmpty());
        assertTrue(auditoria.consultar(adminA, new FiltroAuditoria(null, null, hoy.plusDays(1), null, null), 0, 100)
            .registros().isEmpty());
        assertTrue(auditoria.consultar(adminA, new FiltroAuditoria(null, null, null, hoy.minusDays(1), null), 0, 100)
            .registros().isEmpty());
    }

    /** Pagina de a poco, del mas reciente al mas antiguo, y limita el tamano maximo. */
    @Test
    void paginaYLimitaElTamano() {
        PaginaAuditoria p0 = auditoria.consultar(adminA, SIN_FILTRO, 0, 2);
        assertEquals(2, p0.registros().size());
        assertTrue(p0.totalPaginas() > 1);
        assertTrue(p0.registros().get(0).getId() > p0.registros().get(1).getId(), "del mas reciente al mas antiguo");
        PaginaAuditoria p1 = auditoria.consultar(adminA, SIN_FILTRO, 1, 2);
        assertTrue(p0.registros().stream().noneMatch(r -> p1.registros().contains(r)));
        assertEquals(AuditoriaService.TAMANO_MAXIMO, auditoria.consultar(adminA, SIN_FILTRO, 0, 5000).tamano());
    }

    /** RN-07: inicio de sesion, intento fallido de un usuario existente y cierre de sesion quedan registrados. */
    @Test
    void elLoginQuedaAuditado() {
        assertThrows(NoAutenticadoException.class, () -> autenticacion.iniciarSesion("medico.a", "mala", IP));
        autenticacion.cerrarSesion(medicoA, IP);
        List<String> acciones = auditoria.consultar(adminA, SIN_FILTRO, 0, 100).registros().stream()
            .map(RegistroAuditoria::getAccion).toList();
        assertTrue(acciones.contains(AutenticacionService.ACCION_INICIO_SESION));
        assertTrue(acciones.contains(AutenticacionService.ACCION_LOGIN_FALLIDO));
        assertTrue(acciones.contains(AutenticacionService.ACCION_CIERRE_SESION));
    }

    /** Un usuario que no existe no se puede registrar (la auditoria exige un usuario): se rechaza igual, sin error raro. */
    @Test
    void usuarioInexistenteSeRechazaSinRegistro() {
        long antes = auditoria.consultar(adminA, SIN_FILTRO, 0, 100).total();
        assertThrows(NoAutenticadoException.class, () -> autenticacion.iniciarSesion("nadie", "x", IP));
        assertEquals(antes, auditoria.consultar(adminA, SIN_FILTRO, 0, 100).total());
    }

    /** RN-07: cuando un paciente (sin hospital) consulta su historia, el admin del hospital de esa historia lo ve. */
    @Test
    void laConsultaDelPacienteApareceEnElHospitalDeSuHistoria() {
        Usuario cuenta = usuarios.save(new Usuario(null,
            roles.findByNombre(CodigoRol.PACIENTE.name()).orElseThrow(), "paciente.test.aud", "hash", "ACTIVO"));
        historiaA.getPaciente().vincularUsuario(cuenta);
        portal.miHistorial(cuenta, IP);

        assertTrue(usuariosVistos(adminA, SIN_FILTRO).contains("paciente.test.aud"));
        assertFalse(usuariosVistos(adminB, SIN_FILTRO).contains("paciente.test.aud"));
    }

    @Test
    void listaLasAccionesDelHospital() {
        List<String> acciones = auditoria.accionesDelHospital(adminA);
        assertTrue(acciones.contains(HistorialService.ACCION_CONSULTAR));
        assertTrue(acciones.contains(AutenticacionService.ACCION_INICIO_SESION));
    }
}
