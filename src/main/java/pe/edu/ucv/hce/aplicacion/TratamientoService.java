package pe.edu.ucv.hce.aplicacion;

import org.springframework.stereotype.Service;
import pe.edu.ucv.hce.dominio.Tratamiento;
import pe.edu.ucv.hce.dominio.Usuario;

/**
 * CU-08 Registrar tratamiento e indicaciones (RF-08; RN-02, RN-03, RN-11). Responsable: Mattias (tarea M-2).
 * CONTRATO: Mattias implementa el cuerpo despues del merge de feat/v3-atencion (necesita Atencion y AtencionRepository).
 */
@Service
public class TratamientoService {

    /**
     * Agrega un tratamiento a una atencion ABIERTA del medico en sesion.
     * - Exige Permiso.REGISTRAR_TRATAMIENTO (RN-02): si no, AccesoDenegadoException (403).
     * - Atencion de otro hospital o inexistente: RecursoNoEncontradoException (404) (RN-06).
     * - Atencion cerrada: AtencionCerradaException (409) (RN-03).
     * - Campos vacios: IllegalArgumentException (400).
     * - Audita la accion {@code REGISTRAR_TRATAMIENTO} (RN-07).
     */
    public Tratamiento agregar(Usuario usuario, Integer idAtencion, String indicacion, String medicamento,
                               String dosis, String duracion, String ip) {
        throw new UnsupportedOperationException("Pendiente: CU-08 (tarea M-2, Mattias)");
    }
}
