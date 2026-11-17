package pe.edu.ucv.hce.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * RN-13 (CU-20, CU-21, CU-05): un test por cada transicion permitida y por cada transicion prohibida.
 * ADMITIDO -> EN_ENFERMERIA -> LISTO_PARA_ATENCION -> EN_ATENCION -> ATENCION_CERRADA; CANCELADO solo desde ADMITIDO.
 * Sin Spring: la regla vive en el dominio.
 */
class EstadoAdmisionTest {

    private static final Set<List<EstadoAdmision>> PERMITIDAS = Set.of(
        List.of(EstadoAdmision.ADMITIDO, EstadoAdmision.EN_ENFERMERIA),
        List.of(EstadoAdmision.EN_ENFERMERIA, EstadoAdmision.LISTO_PARA_ATENCION),
        List.of(EstadoAdmision.LISTO_PARA_ATENCION, EstadoAdmision.EN_ATENCION),
        List.of(EstadoAdmision.EN_ATENCION, EstadoAdmision.ATENCION_CERRADA),
        List.of(EstadoAdmision.ADMITIDO, EstadoAdmision.CANCELADO));

    static Stream<Arguments> permitidas() {
        return PERMITIDAS.stream().map(t -> Arguments.of(t.get(0), t.get(1)));
    }

    /** Todos los pares (origen, destino) que NO estan en la lista de permitidas: 36 - 5 = 31 casos. */
    static Stream<Arguments> prohibidas() {
        List<Arguments> casos = new ArrayList<>();
        for (EstadoAdmision origen : EstadoAdmision.values()) {
            for (EstadoAdmision destino : EstadoAdmision.values()) {
                if (!PERMITIDAS.contains(List.of(origen, destino))) {
                    casos.add(Arguments.of(origen, destino));
                }
            }
        }
        return casos.stream();
    }

    /** Lleva una admision nueva hasta el estado pedido usando solo transiciones permitidas. */
    private static Admision admisionEn(EstadoAdmision estado) {
        Establecimiento hospital = new Establecimiento("Hospital de prueba", "0000099", "Lima", "ACTIVO");
        Paciente paciente = new Paciente("DNI", "70000099", "Paciente de prueba", "Estados", LocalDate.of(1990, 1, 1),
            "F", "999111222", "prueba@example.com", "Calle Falsa 123");
        HistoriaClinica historia = new HistoriaClinica(paciente, hospital, "HC-000099", LocalDate.now(), "ABIERTA");
        Admision a = new Admision(historia, Admision.CONSULTA_EXTERNA, null, LocalDateTime.now());
        List<EstadoAdmision> camino = switch (estado) {
            case ADMITIDO -> List.of();
            case CANCELADO -> List.of(EstadoAdmision.CANCELADO);
            case EN_ENFERMERIA -> List.of(EstadoAdmision.EN_ENFERMERIA);
            case LISTO_PARA_ATENCION -> List.of(EstadoAdmision.EN_ENFERMERIA, EstadoAdmision.LISTO_PARA_ATENCION);
            case EN_ATENCION -> List.of(EstadoAdmision.EN_ENFERMERIA, EstadoAdmision.LISTO_PARA_ATENCION,
                EstadoAdmision.EN_ATENCION);
            case ATENCION_CERRADA -> List.of(EstadoAdmision.EN_ENFERMERIA, EstadoAdmision.LISTO_PARA_ATENCION,
                EstadoAdmision.EN_ATENCION, EstadoAdmision.ATENCION_CERRADA);
        };
        camino.forEach(e -> a.pasarA(e, LocalDateTime.now()));
        assertEquals(estado, a.getEstado());
        return a;
    }

    @ParameterizedTest(name = "permitida: {0} -> {1}")
    @MethodSource("permitidas")
    void transicionPermitida(EstadoAdmision origen, EstadoAdmision destino) {
        Admision a = admisionEn(origen);
        assertTrue(origen.puedePasarA(destino));
        a.pasarA(destino, LocalDateTime.now());
        assertEquals(destino, a.getEstado());
    }

    @ParameterizedTest(name = "prohibida: {0} -> {1}")
    @MethodSource("prohibidas")
    void transicionProhibida(EstadoAdmision origen, EstadoAdmision destino) {
        Admision a = admisionEn(origen);
        assertFalse(origen.puedePasarA(destino));
        TransicionInvalidaException e = assertThrows(TransicionInvalidaException.class,
            () -> a.pasarA(destino, LocalDateTime.now()));
        assertTrue(e.getMessage().startsWith("La admisión está"));
        assertEquals(origen, a.getEstado(), "Una transición rechazada no cambia el estado");
    }

    @ParameterizedTest(name = "tipo de atención inválido: {0}")
    @MethodSource("tiposInvalidos")
    void elTipoDeAtencionDebeSerConsultaExternaOEmergencia(String tipo) {
        Establecimiento hospital = new Establecimiento("H", "0000098", "Lima", "ACTIVO");
        HistoriaClinica historia = new HistoriaClinica(null, hospital, "HC-000098", LocalDate.now(), "ABIERTA");
        assertThrows(IllegalArgumentException.class, () -> new Admision(historia, tipo, null, LocalDateTime.now()));
    }

    static Stream<String> tiposInvalidos() {
        return Stream.of("CITA", "", "HOSPITALIZACION");
    }
}
