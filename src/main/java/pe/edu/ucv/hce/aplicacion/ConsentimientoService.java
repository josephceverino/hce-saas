package pe.edu.ucv.hce.aplicacion;

import java.util.List;
import org.springframework.stereotype.Service;
import pe.edu.ucv.hce.dominio.ConsentimientoAcceso;
import pe.edu.ucv.hce.dominio.Usuario;

/**
 * CU-11 Gestionar consentimiento de acceso (RF-17; RN-04, RN-06, RN-07, RN-09). Responsable: Mattias (tarea M-5).
 * CONTRATO: Mattias implementa los cuerpos; la entidad y el repositorio ya existen.
 * Todos los metodos: exigen Permiso.GESTIONAR_CONSENTIMIENTO (solo PACIENTE; otro rol: AccesoDenegadoException, 403)
 * y trabajan SOLO sobre el paciente dueño de la cuenta en sesion (RN-04): se busca con PacienteRepository.findByUsuarioId,
 * nunca con un id de paciente que mande el cliente. Cuenta sin paciente vinculado: AccesoDenegadoException.
 * Cada accion se audita con AuditoriaService (RN-07), sin historia asociada o con la historia del paciente si existe.
 */
@Service
public class ConsentimientoService {

    public static final String ACCION_OTORGAR = "OTORGAR_CONSENTIMIENTO";
    public static final String ACCION_REVOCAR = "REVOCAR_CONSENTIMIENTO";
    public static final String ACCION_LISTAR = "CONSULTAR_CONSENTIMIENTOS";

    /** Todos mis consentimientos (activos y revocados), el mas reciente primero. */
    public List<ConsentimientoAcceso> listar(Usuario usuario, String ip) {
        throw new UnsupportedOperationException("Pendiente: CU-11 (tarea M-5, Mattias)");
    }

    /**
     * Otorga un consentimiento ACTIVO con fecha y alcance (RNF-02, RN-09: finalidad declarada en `alcance`).
     * otorgadoA o alcance vacios: IllegalArgumentException (400).
     */
    public ConsentimientoAcceso otorgar(Usuario usuario, String otorgadoA, String alcance, String ip) {
        throw new UnsupportedOperationException("Pendiente: CU-11 (tarea M-5, Mattias)");
    }

    /**
     * Revoca un consentimiento propio (el acceso del tercero termina de inmediato). Guarda la fecha de revocacion.
     * De otro paciente o inexistente: RecursoNoEncontradoException (404). Ya revocado: IllegalArgumentException (400).
     */
    public ConsentimientoAcceso revocar(Usuario usuario, Integer idConsentimiento, String ip) {
        throw new UnsupportedOperationException("Pendiente: CU-11 (tarea M-5, Mattias)");
    }
}
