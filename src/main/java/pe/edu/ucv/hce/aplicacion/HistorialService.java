package pe.edu.ucv.hce.aplicacion;

import org.springframework.stereotype.Service;
import pe.edu.ucv.hce.dominio.Usuario;

/**
 * CU-06 Consultar historia clinica (RF-06; RN-06, RN-07). Responsable: Pablo (Bloque 2).
 * CONTRATO: la logica se implementa en feat/v3-atencion; hoy solo estan las firmas.
 */
@Service
public class HistorialService {

    /**
     * Carga la historia con sus atenciones, antecedentes y alergias.
     * - Exige Permiso.CONSULTAR_HISTORIA (RN-02/DEC-03): si no, AccesoDenegadoException (403).
     * - Si la historia no existe: RecursoNoEncontradoException (404).
     * - Si es de otro hospital: AccesoDenegadoException (403) (RN-06).
     * - Audita la lectura con accion {@code CONSULTAR_HISTORIA} (RN-07).
     */
    public HistorialClinico consultar(Usuario usuario, Integer idHistoria, String ip) {
        throw new UnsupportedOperationException("Pendiente: CU-06 (Bloque 2)");
    }
}
