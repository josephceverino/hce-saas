package pe.edu.ucv.hce.presentacion;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Ficha M-1 (Mattias): validaciones del formulario de registro de paciente (CU-03, RF-01).
 * Contrato: no se cambia PacienteRequest; solo se prueba con el validador de jakarta.validation.
 * Para trabajar: quita el @Disabled de un test, completalo y haz que pase.
 */
class PacienteRequestValidacionTest {

    static Validator validador;

    @BeforeAll
    static void crearValidador() {
        validador = Validation.buildDefaultValidatorFactory().getValidator();
    }

    /** CP-04 (RF-01): un correo mal escrito produce el mensaje "El correo no es valido.". */
    @Test
    @Disabled("pendiente M-1")
    void correoMalEscritoEsRechazado() {
        // Crea un PacienteRequest valido salvo el correo, valida y comprueba el mensaje.
    }

    /** CP-04 (RF-01): una fecha de nacimiento futura es rechazada. */
    @Test
    @Disabled("pendiente M-1")
    void fechaDeNacimientoFuturaEsRechazada() {
    }

    /** CP-04 (RF-01): la fecha de hoy tambien se rechaza (@Past exige anterior a hoy). */
    @Test
    @Disabled("pendiente M-1")
    void fechaDeNacimientoDeHoyEsRechazada() {
    }

    /** RF-01: los campos obligatorios vacios producen un mensaje en espanol. */
    @Test
    @Disabled("pendiente M-1")
    void camposObligatoriosVaciosSonRechazados() {
    }

    /** RF-01: un formulario completo y correcto no tiene errores. */
    @Test
    @Disabled("pendiente M-1")
    void formularioCorrectoNoTieneErrores() {
    }
}
