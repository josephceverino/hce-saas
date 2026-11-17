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
import pe.edu.ucv.hce.dominio.AtencionCerradaException;
import pe.edu.ucv.hce.dominio.Cie10;
import pe.edu.ucv.hce.dominio.Diagnostico;
import pe.edu.ucv.hce.dominio.HistoriaClinica;
import pe.edu.ucv.hce.dominio.Paciente;
import pe.edu.ucv.hce.dominio.RecursoNoEncontradoException;
import pe.edu.ucv.hce.dominio.Tratamiento;
import pe.edu.ucv.hce.dominio.Usuario;
import pe.edu.ucv.hce.persistencia.RegistroAuditoriaRepository;
import pe.edu.ucv.hce.persistencia.UsuarioRepository;

/**
 * CU-08 (RF-08; RN-02, RN-03, RN-06, RN-07, RN-11). Usa la base real con los usuarios demo, igual que
 * AtencionServiceTest. Version original de Mattias (M-2), rehecha por Pablo para probar las reglas de verdad.
 */
@SpringBootTest
@Transactional
class TratamientoServiceTest {

    private static final String IP = "127.0.0.1";

    @Autowired TratamientoService servicio;
    @Autowired AtencionService atencion;
    @Autowired RegistroPacienteService registro;
    @Autowired UsuarioRepository usuarios;
    @Autowired RegistroAuditoriaRepository auditoria;
    @MockitoBean CatalogoCie10Service catalogo;

    Usuario medicoA;
    Usuario medicoB;
    Usuario enfermeraA;
    HistoriaClinica historiaA;

    @BeforeEach
    void escenario() {
        Usuario admisionA = usuarios.findByUsuario("admision.a").orElseThrow();
        medicoA = usuarios.findByUsuario("medico.a").orElseThrow();
        medicoB = usuarios.findByUsuario("medico.b").orElseThrow();
        enfermeraA = usuarios.findByUsuario("enfermera.a").orElseThrow();
        historiaA = registro.registrarPacienteYAbrirHistoria(new Paciente("DNI", "70000002", "Paciente de prueba",
            "Ficticio", LocalDate.of(1990, 5, 1), "F", "999111222", "prueba@example.com", "Calle Falsa 123"),
            admisionA, IP).historia();
        when(catalogo.porCodigo("J02.9")).thenReturn(
            Optional.of(new Cie10("J02.9", "Faringitis aguda, no especificada")));
    }

    private Integer atencionAbierta() {
        return atencion.crear(medicoA, historiaA.getId(), "Dolor de garganta", "Hace 3 dias", "Faringe roja", IP)
            .atencion().getId();
    }

    private Tratamiento agregar(Usuario u, Integer idAtencion) {
        return servicio.agregar(u, idAtencion, "Reposo e hidratación", "Paracetamol", "500 mg cada 8 horas",
            "5 días", IP);
    }

    private void cerrar(Integer id) {
        atencion.agregarDiagnostico(medicoA, id, "J02.9", Diagnostico.PRINCIPAL, IP);
        atencion.cerrar(medicoA, id, IP);
    }

    /** CU-08: el medico autor agrega un tratamiento a su atencion abierta. */
    @Test
    void medicoAgregaTratamientoAAtencionAbierta() {
        Integer id = atencionAbierta();
        Tratamiento t = agregar(medicoA, id);
        assertEquals("Paracetamol", t.getMedicamento());
        assertEquals(id, t.getAtencion().getId());
        assertEquals(1, atencion.detalle(medicoA, id, IP).tratamientos().size());
    }

    /** RN-02: enfermeria no registra tratamientos (403). */
    @Test
    void enfermeriaNoRegistraTratamiento() {
        Integer id = atencionAbierta();
        assertThrows(AccesoDenegadoException.class, () -> agregar(enfermeraA, id));
    }

    /** RN-03: una atencion cerrada no admite tratamientos (409); se corrige con enmienda. */
    @Test
    void noSeAgregaTratamientoAAtencionCerrada() {
        Integer id = atencionAbierta();
        cerrar(id);
        assertThrows(AtencionCerradaException.class, () -> agregar(medicoA, id));
    }

    /** RN-06: una atencion de otro hospital se trata como inexistente (404), aunque este abierta. */
    @Test
    void otroHospitalNoPuedeAgregarTratamiento() {
        Integer id = atencionAbierta();
        assertThrows(RecursoNoEncontradoException.class, () -> agregar(medicoB, id));
    }

    /** RN-06: tampoco se revela si la atencion de otro hospital esta cerrada (404, no 409). */
    @Test
    void otroHospitalNoSabeSiLaAtencionEstaCerrada() {
        Integer id = atencionAbierta();
        cerrar(id);
        assertThrows(RecursoNoEncontradoException.class, () -> agregar(medicoB, id));
    }

    /** Campos vacios se rechazan con mensaje en español y no se guarda nada. */
    @Test
    void rechazaCamposVacios() {
        Integer id = atencionAbierta();
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
            () -> servicio.agregar(medicoA, id, "", "Paracetamol", "500 mg", "3 días", IP));
        assertEquals("Ingresa la indicación.", e.getMessage());
        assertTrue(atencion.detalle(medicoA, id, IP).tratamientos().isEmpty());
    }

    /** RN-07: la accion queda auditada con el usuario y la ip. */
    @Test
    void laAccionQuedaAuditada() {
        Integer id = atencionAbierta();
        agregar(medicoA, id);
        assertTrue(auditoria.findAll().stream().anyMatch(r ->
            TratamientoService.ACCION_TRATAMIENTO.equals(r.getAccion())
                && medicoA.getId().equals(r.getUsuario().getId()) && IP.equals(r.getIp())));
    }
}
