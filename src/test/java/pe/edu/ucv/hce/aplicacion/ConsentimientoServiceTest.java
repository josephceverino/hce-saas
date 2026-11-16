package pe.edu.ucv.hce.aplicacion;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * CONTRATO de CU-11 (RF-17; RN-04, RN-06, RN-07, RN-09). Lo activa Mattias en la tarea M-5.
 * Para armar el escenario (paciente con cuenta PACIENTE vinculada), mira PortalPacienteServiceTest.
 */
@Disabled("Contrato: se activa en la tarea M-5 (CU-11)")
class ConsentimientoServiceTest {

    /** CP-19: otorgar crea un consentimiento ACTIVO con fecha, quien lo recibe y el alcance. */
    @Test
    void otorgarRegistraFechaYAlcance() { fail("Pendiente"); }

    /** CP-20: revocar pasa a REVOCADO, guarda la fecha y el consentimiento ya no figura activo. */
    @Test
    void revocarCortaElAcceso() { fail("Pendiente"); }

    /** Revocar dos veces el mismo consentimiento se rechaza (IllegalArgumentException) y no pisa la fecha. */
    @Test
    void noSeRevocaDosVeces() { fail("Pendiente"); }

    /** RN-04: el paciente A no ve ni revoca los consentimientos del paciente B (404 al revocar). */
    @Test
    void unPacienteNoToca_losConsentimientosDeOtro() { fail("Pendiente"); }

    /** RN-02/DEC-03: solo el rol PACIENTE gestiona consentimientos; medico o admision: AccesoDenegadoException. */
    @Test
    void soloElPacienteGestionaConsentimientos() { fail("Pendiente"); }

    /** Campos vacios (otorgadoA, alcance) se rechazan con mensaje en español. */
    @Test
    void rechazaCamposVacios() { fail("Pendiente"); }

    /** RN-07: otorgar y revocar quedan auditados con usuario e IP. */
    @Test
    void lasAccionesQuedanAuditadas() { fail("Pendiente"); }
}
