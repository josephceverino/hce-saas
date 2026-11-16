package pe.edu.ucv.hce.aplicacion;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/** CONTRATO de CU-06 (RF-06; RN-02, RN-06, RN-07). Pablo lo activa en feat/v3-atencion. */
@Disabled("Contrato: se activa al implementar CU-06 (Bloque 2)")
class HistorialServiceTest {

    /** CU-06: devuelve la historia con alergias, antecedentes y atenciones (la mas reciente primero). */
    @Test
    void medicoConsultaLaHistoriaDeSuHospital() { fail("Pendiente"); }

    /** RN-02/DEC-03: enfermeria tambien consulta; admision no (AccesoDenegadoException). */
    @Test
    void admisionNoConsultaHistoriaClinica() { fail("Pendiente"); }

    /** RN-06 (RNF-06): el medico del hospital B no ve la historia del hospital A: AccesoDenegadoException. */
    @Test
    void unHospitalNoVeLaHistoriaDeOtro() { fail("Pendiente"); }

    /** Historia inexistente: RecursoNoEncontradoException. */
    @Test
    void historiaInexistente() { fail("Pendiente"); }

    /** RN-07: cada lectura exitosa queda auditada con accion CONSULTAR_HISTORIA. */
    @Test
    void laLecturaQuedaAuditada() { fail("Pendiente"); }
}
