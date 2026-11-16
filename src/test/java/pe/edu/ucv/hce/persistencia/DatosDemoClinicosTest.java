package pe.edu.ucv.hce.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pe.edu.ucv.hce.aplicacion.HistorialClinico;
import pe.edu.ucv.hce.aplicacion.HistorialService;
import pe.edu.ucv.hce.dominio.AccesoDenegadoException;
import pe.edu.ucv.hce.dominio.Atencion;
import pe.edu.ucv.hce.dominio.HistoriaClinica;

/**
 * Semilla de la demo (hce.demo-datos=true). Usa su propia base en memoria para no mezclarse con los demas tests.
 * Comprueba que las cifras de los tableros tendran datos y que los hospitales siguen aislados (RN-06).
 */
@SpringBootTest(properties = {"hce.demo-datos=true", "spring.datasource.url=jdbc:h2:mem:demo-datos-test"})
class DatosDemoClinicosTest {

    @Autowired HistoriaClinicaRepository historias;
    @Autowired AtencionRepository atenciones;
    @Autowired UsuarioRepository usuarios;
    @Autowired HistorialService historial;

    @Test
    void cargaPacientesYAtencionesDeAmbosHospitales() {
        Integer hospitalA = usuarios.findByUsuario("medico.a").orElseThrow().getEstablecimiento().getId();
        Integer hospitalB = usuarios.findByUsuario("medico.b").orElseThrow().getEstablecimiento().getId();
        assertEquals(4, historias.findAllByEstablecimientoIdOrderByIdDesc(hospitalA).size());
        assertEquals(2, historias.findAllByEstablecimientoIdOrderByIdDesc(hospitalB).size());
        long cerradas = atenciones.findAll().stream().filter(Atencion::estaCerrada).count();
        long abiertas = atenciones.findAll().stream().filter(a -> !a.estaCerrada()).count();
        assertTrue(cerradas > 0 && abiertas > 0, "debe haber atenciones cerradas y abiertas");
    }

    /** RN-06: el medico B no puede abrir una historia sembrada en el hospital A. */
    @Test
    void laSemillaRespetaElAislamientoEntreHospitales() {
        var medicoA = usuarios.findByUsuario("medico.a").orElseThrow();
        var medicoB = usuarios.findByUsuario("medico.b").orElseThrow();
        HistoriaClinica deA = historias.findAllByEstablecimientoIdOrderByIdDesc(medicoA.getEstablecimiento().getId()).get(0);
        HistorialClinico h = historial.consultar(medicoA, deA.getId(), "127.0.0.1");
        assertEquals(deA.getId(), h.historia().getId());
        assertThrows(AccesoDenegadoException.class, () -> historial.consultar(medicoB, deA.getId(), "127.0.0.1"));
    }
}
