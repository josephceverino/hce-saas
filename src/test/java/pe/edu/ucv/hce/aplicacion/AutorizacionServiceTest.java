package pe.edu.ucv.hce.aplicacion;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import pe.edu.ucv.hce.dominio.AccesoDenegadoException;
import pe.edu.ucv.hce.dominio.CodigoRol;
import pe.edu.ucv.hce.dominio.Rol;
import pe.edu.ucv.hce.dominio.Usuario;

/** DEC-03 (RF-12; RN-02): la matriz de docs/roles_permisos.md aplicada por AutorizacionService. Sin Spring. */
class AutorizacionServiceTest {

    private final AutorizacionService autorizacion = new AutorizacionService();

    private static Usuario con(CodigoRol codigo) {
        return new Usuario(null, new Rol(codigo.name(), codigo.descripcion()), "prueba", "hash", "ACTIVO");
    }

    /** RF-01: solo ADMISION registra pacientes. */
    @Test
    void soloAdmisionRegistraPacientes() {
        assertTrue(autorizacion.puede(con(CodigoRol.ADMISION), Permiso.REGISTRAR_PACIENTE));
        assertFalse(autorizacion.puede(con(CodigoRol.MEDICO), Permiso.REGISTRAR_PACIENTE));
        assertFalse(autorizacion.puede(con(CodigoRol.PACIENTE), Permiso.REGISTRAR_PACIENTE));
    }

    /** RN-02 / RF-05: solo el medico registra atenciones; enfermeria y admision no. */
    @Test
    void soloMedicoRegistraAtenciones() {
        assertTrue(autorizacion.puede(con(CodigoRol.MEDICO), Permiso.REGISTRAR_ATENCION));
        assertFalse(autorizacion.puede(con(CodigoRol.ENFERMERIA), Permiso.REGISTRAR_ATENCION));
        assertFalse(autorizacion.puede(con(CodigoRol.ADMISION), Permiso.REGISTRAR_ATENCION));
    }

    /** RF-06: medico y enfermeria consultan historias; admision no. */
    @Test
    void consultarHistoriaEsDeMedicoYEnfermeria() {
        assertTrue(autorizacion.puede(con(CodigoRol.MEDICO), Permiso.CONSULTAR_HISTORIA));
        assertTrue(autorizacion.puede(con(CodigoRol.ENFERMERIA), Permiso.CONSULTAR_HISTORIA));
        assertFalse(autorizacion.puede(con(CodigoRol.ADMISION), Permiso.CONSULTAR_HISTORIA));
    }

    /** RN-04 / RF-15: el paciente ve su historial pero no registra nada clinico. */
    @Test
    void pacienteSoloTienePermisosDelPortal() {
        Usuario paciente = con(CodigoRol.PACIENTE);
        assertTrue(autorizacion.puede(paciente, Permiso.VER_MI_HISTORIAL));
        assertTrue(autorizacion.puede(paciente, Permiso.GESTIONAR_CONSENTIMIENTO));
        assertFalse(autorizacion.puede(paciente, Permiso.REGISTRAR_ATENCION));
        assertFalse(autorizacion.puede(paciente, Permiso.CONSULTAR_HISTORIA));
    }

    /** DEC-03: un rol sin codigo valido no tiene ningun permiso. */
    @Test
    void rolDesconocidoNoTienePermisos() {
        Usuario raro = new Usuario(null, new Rol("Personal administrativo", "rol antiguo"), "x", "h", "ACTIVO");
        for (Permiso p : Permiso.values()) {
            assertFalse(autorizacion.puede(raro, p), p.name());
        }
    }

    /** exigir lanza 403 sin permiso y no hace nada con permiso. */
    @Test
    void exigirLanzaAccesoDenegado() {
        assertThrows(AccesoDenegadoException.class,
            () -> autorizacion.exigir(con(CodigoRol.MEDICO), Permiso.REGISTRAR_PACIENTE));
        assertDoesNotThrow(() -> autorizacion.exigir(con(CodigoRol.ADMISION), Permiso.REGISTRAR_PACIENTE));
    }
}
