package pe.edu.ucv.hce.aplicacion;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * CONTRATO de CU-05 (RF-05; RN-02, RN-03, RN-06, RN-07, RN-10, RN-11). Pablo quita @Disabled y los implementa
 * en feat/v3-atencion. Cada metodo describe el comportamiento que debe cumplirse.
 */
@Disabled("Contrato: se activa al implementar CU-05 (Bloque 2)")
class AtencionServiceTest {

    /** RN-11: la atencion queda ligada a la historia, al profesional del usuario y a su hospital; estado ABIERTA. */
    @Test
    void medicoCreaAtencionAbierta() { fail("Pendiente"); }

    /** RN-02: enfermeria (y admision) no pueden crear atenciones: AccesoDenegadoException. */
    @Test
    void enfermeriaNoPuedeCrearAtencion() { fail("Pendiente"); }

    /** RN-06: el medico del hospital B no puede crear atenciones en una historia del hospital A. */
    @Test
    void medicoDeOtroHospitalNoPuedeAtender() { fail("Pendiente"); }

    /** RN-10: un codigo fuera del catalogo CIE-10 se rechaza con DiagnosticoInvalidoException. */
    @Test
    void rechazaDiagnosticoFueraDelCatalogo() { fail("Pendiente"); }

    /** CU-05: una atencion admite varios diagnosticos (uno PRINCIPAL y otros SECUNDARIO). */
    @Test
    void admiteVariosDiagnosticos() { fail("Pendiente"); }

    /** CU-05: cerrar sin ningun diagnostico se rechaza (IllegalArgumentException). */
    @Test
    void noCierraSinDiagnostico() { fail("Pendiente"); }

    /** RN-03: tras cerrar, agregar diagnostico o volver a cerrar lanza AtencionCerradaException. */
    @Test
    void atencionCerradaEsInmutable() { fail("Pendiente"); }

    /** RN-03: la enmienda sobre una atencion cerrada guarda autor y fecha y no cambia la atencion original. */
    @Test
    void enmiendaSobreAtencionCerrada() { fail("Pendiente"); }

    /** RN-03: no se enmienda una atencion que sigue ABIERTA. */
    @Test
    void noSeEnmiendaAtencionAbierta() { fail("Pendiente"); }

    /** RN-07: crear, cerrar y enmendar dejan registro de auditoria con el usuario y la IP. */
    @Test
    void lasAccionesQuedanAuditadas() { fail("Pendiente"); }
}
