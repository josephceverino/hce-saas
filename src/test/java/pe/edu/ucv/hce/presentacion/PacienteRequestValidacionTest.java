package pe.edu.ucv.hce.presentacion;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Ficha M-1 (Mattias): validaciones del formulario de registro de paciente (CU-03, RF-01).
 * Contrato: no se cambia PacienteRequest; solo se prueba con el validador de jakarta.validation.
 */
class PacienteRequestValidacionTest {

    static Validator validador;

    @BeforeAll
    static void crearValidador() {
        validador = Validation.buildDefaultValidatorFactory().getValidator();
    }

    /** Plan v3, 1.3: filiacion completa valida (lugar de nacimiento, procedencia, estado civil, instruccion,
     * ocupacion, religion y contacto de emergencia). */
    private static final String[] FILIACION = {"Trujillo", "Trujillo", "SOLTERO", "SECUNDARIA", "Comerciante",
        "", "Maria Perez", "Madre", "987111222"};

    /** Plan v3, 1.3 (RF-01): sin contacto de emergencia no se registra. */
    @Test
    void sinContactoDeEmergenciaEsRechazado() {
        PacienteRequest req = new PacienteRequest("DNI", "12345678", "Juan", "Perez", LocalDate.of(1990, 5, 10), "M",
            "987654321", "juan.perez@demo.com", "Av. Peru 123", "Trujillo", "Trujillo", "SOLTERO", "SECUNDARIA",
            "Comerciante", "", "", "", "");
        assertTrue(validador.validate(req).stream().anyMatch(v -> v.getMessage().contains("contacto de emergencia")));
    }

    /** CP-04 (RF-01): un correo mal escrito produce el mensaje "El correo no es valido.". */
    @Test
    void correoMalEscritoEsRechazado() {
        PacienteRequest req = new PacienteRequest(
            "DNI", "12345678", "Juan", "Perez", 
            LocalDate.of(1990, 5, 10), "M", "987654321", 
            "correo-invalido", "Av. Peru 123", FILIACION[0], FILIACION[1], FILIACION[2], FILIACION[3],
            FILIACION[4], FILIACION[5], FILIACION[6], FILIACION[7], FILIACION[8]
        );

        Set<ConstraintViolation<PacienteRequest>> violaciones = validador.validate(req);
        assertFalse(violaciones.isEmpty());
        boolean mensajeEsperado = violaciones.stream()
                .anyMatch(v -> v.getMessage().contains("valido") || v.getMessage().contains("correo"));
        assertTrue(mensajeEsperado);
    }

    /** CP-04 (RF-01): una fecha de nacimiento futura es rechazada. */
    @Test
    void fechaDeNacimientoFuturaEsRechazada() {
        PacienteRequest req = new PacienteRequest(
            "DNI", "12345678", "Juan", "Perez", 
            LocalDate.now().plusDays(1), "M", "987654321", 
            "juan.perez@demo.com", "Av. Peru 123", FILIACION[0], FILIACION[1], FILIACION[2], FILIACION[3],
            FILIACION[4], FILIACION[5], FILIACION[6], FILIACION[7], FILIACION[8]
        );

        Set<ConstraintViolation<PacienteRequest>> violaciones = validador.validate(req);
        assertFalse(violaciones.isEmpty());
    }

    /** CP-04 (RF-01): la fecha de hoy tambien se rechaza (@Past exige anterior a hoy). */
    @Test
    void fechaDeNacimientoDeHoyEsRechazada() {
        PacienteRequest req = new PacienteRequest(
            "DNI", "12345678", "Juan", "Perez", 
            LocalDate.now(), "M", "987654321", 
            "juan.perez@demo.com", "Av. Peru 123", FILIACION[0], FILIACION[1], FILIACION[2], FILIACION[3],
            FILIACION[4], FILIACION[5], FILIACION[6], FILIACION[7], FILIACION[8]
        );

        Set<ConstraintViolation<PacienteRequest>> violaciones = validador.validate(req);
        assertFalse(violaciones.isEmpty());
    }

    /** RF-01: los campos obligatorios vacios producen un mensaje. */
    @Test
    void camposObligatoriosVaciosSonRechazados() {
        PacienteRequest req = new PacienteRequest(
            "", "", "", "", 
            null, "", "", "", "", "", "", "", "", "", "", "", "", ""
        );
        
        Set<ConstraintViolation<PacienteRequest>> violaciones = validador.validate(req);
        assertFalse(violaciones.isEmpty());
    }

    /** RF-01: un formulario completo y correcto no tiene errores. */
    @Test
    void formularioCorrectoNoTieneErrores() {
        PacienteRequest req = new PacienteRequest(
            "DNI", "12345678", "Juan", "Perez", 
            LocalDate.of(1990, 5, 10), "M", "987654321", 
            "juan.perez@demo.com", "Av. Peru 123", FILIACION[0], FILIACION[1], FILIACION[2], FILIACION[3],
            FILIACION[4], FILIACION[5], FILIACION[6], FILIACION[7], FILIACION[8]
        );

        Set<ConstraintViolation<PacienteRequest>> violaciones = validador.validate(req);
        assertTrue(violaciones.isEmpty());
    }
}