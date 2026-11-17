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
import pe.edu.ucv.hce.aplicacion.Indicadores.Tarjeta;
import pe.edu.ucv.hce.dominio.AccesoDenegadoException;
import pe.edu.ucv.hce.dominio.Cie10;
import pe.edu.ucv.hce.dominio.CodigoRol;
import pe.edu.ucv.hce.dominio.Diagnostico;
import pe.edu.ucv.hce.dominio.HistoriaClinica;
import pe.edu.ucv.hce.dominio.Paciente;
import pe.edu.ucv.hce.dominio.Usuario;
import pe.edu.ucv.hce.persistencia.RolRepository;
import pe.edu.ucv.hce.persistencia.UsuarioRepository;

/**
 * Indicadores reales (RF-19, RF-20; DEC-10, DEC-11) y aislamiento entre hospitales (RN-06).
 * Los tests corren sin datos de demostracion, asi que las cifras se pueden verificar exactamente.
 */
@SpringBootTest
@Transactional
class IndicadoresServiceTest {

    private static final String IP = "127.0.0.1";

    @Autowired IndicadoresService indicadores;
    @Autowired AtencionService atenciones;
    @Autowired HistorialService historial;
    @Autowired RegistroPacienteService registro;
    @Autowired UsuarioRepository usuarios;
    @Autowired RolRepository roles;
    @MockitoBean CatalogoCie10Service catalogo;

    Usuario admisionA;
    Usuario admisionB;
    Usuario medicoA;
    Usuario medicoB;
    Usuario adminA;
    Usuario enfermeraA;
    HistoriaClinica h1;
    HistoriaClinica h2;

    @BeforeEach
    void escenario() {
        admisionA = usuarios.findByUsuario("admision.a").orElseThrow();
        admisionB = usuarios.findByUsuario("admision.b").orElseThrow();
        medicoA = usuarios.findByUsuario("medico.a").orElseThrow();
        medicoB = usuarios.findByUsuario("medico.b").orElseThrow();
        adminA = usuarios.findByUsuario("admin.a").orElseThrow();
        enfermeraA = usuarios.findByUsuario("enfermera.a").orElseThrow();
        when(catalogo.porCodigo("J02.9")).thenReturn(
            Optional.of(new Cie10("J02.9", "Faringitis aguda, no especificada")));
        when(catalogo.porCodigo("I10")).thenReturn(Optional.of(new Cie10("I10", "Hipertensión esencial (primaria)")));

        // Hospital A: 2 pacientes, 3 atenciones (2 cerradas con J02.9 e I10... ver abajo, 1 abierta). Hospital B: 1 paciente.
        h1 = registro.registrarPacienteYAbrirHistoria(paciente("70000041"), admisionA, IP).historia();
        h2 = registro.registrarPacienteYAbrirHistoria(paciente("70000042"), admisionA, IP).historia();
        registro.registrarPacienteYAbrirHistoria(paciente("70000043"), admisionB, IP);

        cerrada(h1, "J02.9");
        cerrada(h2, "J02.9");
        Integer abierta = atenciones.crear(medicoA, h1.getId(), "Borrador", "a", "e", IP).atencion().getId();
        atenciones.agregarDiagnostico(medicoA, abierta, "I10", Diagnostico.PRINCIPAL, IP);
        historial.consultar(medicoA, h1.getId(), IP);
    }

    private Paciente paciente(String dni) {
        return new Paciente("DNI", dni, "Paciente de prueba", "Indicadores", LocalDate.of(1990, 1, 1), "F",
            "999111222", "p@example.com", "Calle Falsa 123");
    }

    private void cerrada(HistoriaClinica h, String cie) {
        Integer id = atenciones.crear(medicoA, h.getId(), "Consulta", "a", "e", IP).atencion().getId();
        atenciones.agregarDiagnostico(medicoA, id, cie, Diagnostico.PRINCIPAL, IP);
        atenciones.cerrar(medicoA, id, IP);
    }

    private long valor(Indicadores.Inicio inicio, String clave) {
        return inicio.tarjetas().stream().filter(t -> t.clave().equals(clave)).map(Tarjeta::valor).findFirst()
            .orElseThrow(() -> new AssertionError("falta la tarjeta " + clave));
    }

    /** Cifras reales del hospital A para Admisión. */
    @Test
    void inicioDeAdmision() {
        var inicio = indicadores.inicio(admisionA);
        assertEquals(2, valor(inicio, "pacientes"));
        assertEquals(2, valor(inicio, "historias-abiertas"));
        assertEquals(2, valor(inicio, "registrados-hoy"));
    }

    /** RN-06: el hospital B cuenta solo lo suyo. */
    @Test
    void cadaHospitalVeSoloSusCifras() {
        assertEquals(1, valor(indicadores.inicio(admisionB), "pacientes"));
        assertEquals(2, valor(indicadores.inicio(admisionA), "pacientes"));
    }

    /** El médico ve sus atenciones de hoy y las abiertas por cerrar (lo que más le sirve). */
    @Test
    void inicioDelMedico() {
        var inicio = indicadores.inicio(medicoA);
        assertEquals(3, valor(inicio, "mis-atenciones-hoy"));
        assertEquals(1, valor(inicio, "abiertas-por-cerrar"));
        var otro = indicadores.inicio(medicoB);
        assertEquals(0, valor(otro, "mis-atenciones-hoy"));
        assertEquals(0, valor(otro, "abiertas-por-cerrar"));
    }

    @Test
    void inicioDeEnfermeriaYDelAdmin() {
        assertEquals(2, valor(indicadores.inicio(enfermeraA), "pacientes"));
        var admin = indicadores.inicio(adminA);
        assertEquals(3, valor(admin, "atenciones-hoy"));
        assertEquals(1, valor(admin, "atenciones-abiertas"));
        assertTrue(valor(admin, "usuarios") >= 1);
    }

    /** Paciente y plataforma no usan el Inicio de un hospital. */
    @Test
    void otrosRolesNoUsanElInicioDeHospital() {
        Usuario plataforma = usuarios.findByUsuario("plataforma").orElseThrow();
        assertThrows(AccesoDenegadoException.class, () -> indicadores.inicio(plataforma));
    }

    /** CU-23: apoyo al diagnostico ve cifras reales de examenes (en cero si no hay). */
    @Test
    void apoyoAlDiagnosticoVeCifrasRealesDeExamenes() {
        Usuario apoyo = usuarios.save(new Usuario(adminA.getEstablecimiento(),
            roles.findByNombre(CodigoRol.APOYO_DIAGNOSTICO.name()).orElseThrow(), "apoyo.inicio.test", "hash", "ACTIVO"));
        var inicio = indicadores.inicio(apoyo);
        assertEquals(adminA.getEstablecimiento().getNombre(), inicio.establecimiento());
        assertEquals(java.util.List.of(0L, 0L), inicio.tarjetas().stream().map(Indicadores.Tarjeta::valor).toList());
    }

    /** Tablero del admin: 14 días y 8 semanas con ceros, top de diagnósticos, abiertas y cerradas y últimos accesos. */
    @Test
    void tableroDelAdminDelHospital() {
        var t = indicadores.tablero(adminA);
        assertEquals(IndicadoresService.DIAS_GRAFICO, t.atencionesPorDia().size());
        assertEquals(3, t.atencionesPorDia().get(t.atencionesPorDia().size() - 1).total());
        assertEquals(LocalDate.now(), t.atencionesPorDia().get(t.atencionesPorDia().size() - 1).fecha());
        assertEquals(IndicadoresService.SEMANAS_GRAFICO, t.pacientesNuevosPorSemana().size());
        assertEquals(2, t.pacientesNuevosPorSemana().stream().mapToLong(Indicadores.PuntoFecha::total).sum());
        assertEquals(1, t.atencionesAbiertas());
        assertEquals(2, t.atencionesCerradas());
        assertEquals("J02.9", t.diagnosticosFrecuentes().get(0).codigoCie10());
        assertEquals(2, t.diagnosticosFrecuentes().get(0).total());
        assertFalse(t.ultimosAccesos().isEmpty());
        assertTrue(t.ultimosAccesos().size() <= IndicadoresService.ULTIMOS_ACCESOS);
        assertTrue(t.ultimosAccesos().stream().allMatch(a -> a.nroHc() != null));
    }

    /** Un hospital sin atenciones: el gráfico mantiene sus días en cero (estado vacío, nunca inventado). */
    @Test
    void elTableroDeUnHospitalSinAtencionesVaEnCeros() {
        Usuario adminB = usuarios.save(new Usuario(medicoB.getEstablecimiento(),
            roles.findByNombre(CodigoRol.ADMIN_HOSPITAL.name()).orElseThrow(), "admin.test.b", "hash", "ACTIVO"));
        var t = indicadores.tablero(adminB);
        assertEquals(IndicadoresService.DIAS_GRAFICO, t.atencionesPorDia().size());
        assertEquals(0, t.atencionesPorDia().stream().mapToLong(Indicadores.PuntoFecha::total).sum());
        assertEquals(0, t.atencionesAbiertas() + t.atencionesCerradas());
        assertTrue(t.diagnosticosFrecuentes().isEmpty());
        // el unico acceso del hospital B es el registro de su paciente; no aparece nada del hospital A (RN-06)
        assertEquals(1, t.ultimosAccesos().size());
        assertEquals(RegistroPacienteService.ACCION_REGISTRAR, t.ultimosAccesos().get(0).accion());
        assertEquals(1, t.pacientesNuevosPorSemana().stream().mapToLong(Indicadores.PuntoFecha::total).sum());
    }

    /** RF-19: el tablero es solo del Administrador del hospital. */
    @Test
    void soloElAdminVeElTablero() {
        assertThrows(AccesoDenegadoException.class, () -> indicadores.tablero(medicoA));
        assertThrows(AccesoDenegadoException.class, () -> indicadores.tablero(admisionA));
    }

    /** RF-20 (DEC-11): el panel de plataforma da solo totales por hospital y solo lo ve plataforma. */
    @Test
    void panelDePlataformaSoloConTotales() {
        Usuario plataforma = usuarios.findByUsuario("plataforma").orElseThrow();
        var lista = indicadores.hospitales(plataforma);
        var a = lista.stream().filter(h -> h.nombre().equals("Hospital Demo A")).findFirst().orElseThrow();
        var b = lista.stream().filter(h -> h.nombre().equals("Hospital Demo B")).findFirst().orElseThrow();
        assertEquals(2, a.pacientes());
        assertEquals(3, a.atenciones());
        assertEquals(1, b.pacientes());
        assertEquals(0, b.atenciones());
        assertTrue(a.usuarios() >= 1);
        assertThrows(AccesoDenegadoException.class, () -> indicadores.hospitales(adminA));
        assertThrows(AccesoDenegadoException.class, () -> indicadores.hospitales(medicoA));
    }
}
