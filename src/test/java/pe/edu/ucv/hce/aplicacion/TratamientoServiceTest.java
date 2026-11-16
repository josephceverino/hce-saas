package pe.edu.ucv.hce.aplicacion;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/** CONTRATO de CU-08 (RF-08; RN-02, RN-03, RN-06, RN-07, RN-11). Lo activa Mattias en la tarea M-2. */
@Disabled("Contrato: se activa en la tarea M-2 (CU-08), despues del merge de feat/v3-atencion")
class TratamientoServiceTest {

    /** El medico que abrio la atencion agrega un tratamiento y queda vinculado a ella. */
    @Test
    void medicoAgregaTratamientoAAtencionAbierta() { fail("Pendiente"); }

    /** RN-02: enfermeria no registra tratamientos: AccesoDenegadoException. */
    @Test
    void enfermeriaNoRegistraTratamiento() { fail("Pendiente"); }

    /** RN-03: en una atencion cerrada se lanza AtencionCerradaException (hay que usar enmienda). */
    @Test
    void noSeAgregaTratamientoAAtencionCerrada() { fail("Pendiente"); }

    /** RN-06: el medico del hospital B no puede agregar tratamiento a una atencion del hospital A. */
    @Test
    void otroHospitalNoPuedeAgregarTratamiento() { fail("Pendiente"); }

    /** Los campos vacios se rechazan con mensaje en espanol. */
    @Test
    void rechazaCamposVacios() { fail("Pendiente"); }

    /** RN-07: la accion queda auditada como REGISTRAR_TRATAMIENTO. */
    @Test
    void laAccionQuedaAuditada() { fail("Pendiente"); }
}
