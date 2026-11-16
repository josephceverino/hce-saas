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
import pe.edu.ucv.hce.dominio.Antecedente;
import pe.edu.ucv.hce.dominio.Cie10;
import pe.edu.ucv.hce.dominio.CodigoRol;
import pe.edu.ucv.hce.dominio.Diagnostico;
import pe.edu.ucv.hce.dominio.HistoriaClinica;
import pe.edu.ucv.hce.dominio.Paciente;
import pe.edu.ucv.hce.dominio.RecursoNoEncontradoException;
import pe.edu.ucv.hce.dominio.Usuario;
import pe.edu.ucv.hce.persistencia.AlergiaRepository;
import pe.edu.ucv.hce.persistencia.AntecedenteRepository;
import pe.edu.ucv.hce.persistencia.RegistroAuditoriaRepository;
import pe.edu.ucv.hce.persistencia.RolRepository;
import pe.edu.ucv.hce.persistencia.UsuarioRepository;

/** CU-06 (RF-06; RN-02, RN-06, RN-07) y aislamiento entre hospitales (RNF-06). */
@SpringBootTest
@Transactional
class HistorialServiceTest {

    private static final String IP = "127.0.0.1";

    @Autowired HistorialService servicio;
    @Autowired AtencionService atencionServicio;
    @Autowired RegistroPacienteService registro;
    @Autowired UsuarioRepository usuarios;
    @Autowired RolRepository roles;
    @Autowired AntecedenteRepository antecedentes;
    @Autowired AlergiaRepository alergias;
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
        historiaA = registro.registrarPacienteYAbrirHistoria(new Paciente("DNI", "70000002", "Paciente de prueba",
            "Ficticio", LocalDate.of(1985, 3, 2), "M", "999111222", "prueba@example.com", "Calle Falsa 123"),
            admisionA, IP).historia();
        when(catalogo.porCodigo("J02.9")).thenReturn(
            Optional.of(new Cie10("J02.9", "Faringitis aguda, no especificada")));
    }

    /** CU-06: historia con alergias, antecedentes y atenciones (la mas reciente primero). */
    @Test
    void medicoConsultaLaHistoriaDeSuHospital() {
        alergias.save(new Alergia(historiaA, "Penicilina", "Urticaria", "ALTA"));
        antecedentes.save(new Antecedente(historiaA, "Personal", "Asma leve", LocalDate.now()));
        Integer primera = atencionServicio.crear(medicoA, historiaA.getId(), "Primera", "a", "e", IP).atencion().getId();
        atencionServicio.agregarDiagnostico(medicoA, primera, "J02.9", Diagnostico.PRINCIPAL, IP);
        atencionServicio.crear(medicoA, historiaA.getId(), "Segunda", "a", "e", IP);

        HistorialClinico h = servicio.consultar(medicoA, historiaA.getId(), IP);

        assertEquals(historiaA.getId(), h.historia().getId());
        assertEquals(1, h.alergias().size());
        assertEquals(1, h.antecedentes().size());
        assertEquals(2, h.atenciones().size());
        assertEquals(1, h.atenciones().stream()
            .filter(a -> a.atencion().getId().equals(primera)).findFirst().orElseThrow().diagnosticos().size());
    }

    /** RN-02/DEC-03: enfermeria consulta; admision no. */
    @Test
    void enfermeriaConsultaYAdmisionNo() {
        Usuario enfermera = usuarios.save(new Usuario(medicoA.getEstablecimiento(),
            roles.findByNombre(CodigoRol.ENFERMERIA.name()).orElseThrow(), "enfermera.prueba.h", "hash", "ACTIVO"));
        assertEquals(historiaA.getId(), servicio.consultar(enfermera, historiaA.getId(), IP).historia().getId());
        assertThrows(AccesoDenegadoException.class, () -> servicio.consultar(admisionA, historiaA.getId(), IP));
    }

    /** RN-06 (RNF-06): el hospital B no ve la historia del hospital A. Test exigido por CLAUDE.md. */
    @Test
    void unHospitalNoVeLaHistoriaDeOtro() {
        assertThrows(AccesoDenegadoException.class, () -> servicio.consultar(medicoB, historiaA.getId(), IP));
    }

    @Test
    void historiaInexistente() {
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.consultar(medicoA, 999999, IP));
    }

    /** RN-07: la lectura exitosa queda auditada; el intento denegado no registra una lectura. */
    @Test
    void laLecturaQuedaAuditada() {
        assertThrows(AccesoDenegadoException.class, () -> servicio.consultar(medicoB, historiaA.getId(), IP));
        long antes = auditoria.findAllByHistoriaClinicaIdOrderByIdDesc(historiaA.getId()).stream()
            .filter(r -> HistorialService.ACCION_CONSULTAR.equals(r.getAccion())).count();
        assertEquals(0, antes);

        servicio.consultar(medicoA, historiaA.getId(), IP);

        var registros = auditoria.findAllByHistoriaClinicaIdOrderByIdDesc(historiaA.getId());
        assertTrue(registros.stream().anyMatch(r -> HistorialService.ACCION_CONSULTAR.equals(r.getAccion())
            && r.getUsuario().getId().equals(medicoA.getId()) && IP.equals(r.getIp())));
    }
}
