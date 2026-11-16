package pe.edu.ucv.hce.aplicacion;

import org.springframework.stereotype.Service;
import pe.edu.ucv.hce.dominio.Atencion;
import pe.edu.ucv.hce.dominio.Diagnostico;
import pe.edu.ucv.hce.dominio.Enmienda;
import pe.edu.ucv.hce.dominio.Usuario;

/**
 * CU-05 Registrar atencion medica (RF-05; RN-02, RN-03, RN-06, RN-07, RN-10, RN-11). Responsable: Pablo (Bloque 2).
 * CONTRATO: la logica se implementa en feat/v3-atencion; hoy solo estan las firmas.
 * Todos los metodos exigen Permiso.REGISTRAR_ATENCION y auditan la accion (RN-07).
 * El profesional de la atencion es el del usuario en sesion (RN-11); el hospital es el del usuario (RN-06).
 */
@Service
public class AtencionService {

    /** Crea una atencion ABIERTA en la historia indicada. Historia de otro hospital: AccesoDenegadoException. */
    public Atencion crear(Usuario usuario, Integer idHistoria, String motivo, String anamnesis,
                          String examenFisico, String ip) {
        throw new UnsupportedOperationException("Pendiente: CU-05 (Bloque 2)");
    }

    /**
     * Agrega un diagnostico a una atencion ABIERTA. El codigo debe existir en el catalogo CIE-10 (RN-10):
     * si no, DiagnosticoInvalidoException (400). Atencion cerrada: AtencionCerradaException (409).
     * tipo: Diagnostico.PRINCIPAL o Diagnostico.SECUNDARIO.
     */
    public Diagnostico agregarDiagnostico(Usuario usuario, Integer idAtencion, String codigoCie10,
                                          String tipo, String ip) {
        throw new UnsupportedOperationException("Pendiente: CU-05 (Bloque 2)");
    }

    /**
     * Cierra la atencion (RN-03): queda inmutable. Debe tener al menos un diagnostico; si no,
     * IllegalArgumentException (400). Ya cerrada: AtencionCerradaException (409).
     */
    public Atencion cerrar(Usuario usuario, Integer idAtencion, String ip) {
        throw new UnsupportedOperationException("Pendiente: CU-05 (Bloque 2)");
    }

    /**
     * Registra una enmienda (texto libre) sobre una atencion CERRADA, con autor y fecha (RN-03).
     * Si la atencion sigue ABIERTA no se enmienda: IllegalArgumentException (400).
     */
    public Enmienda enmendar(Usuario usuario, Integer idAtencion, String motivo,
                             String contenidoCorregido, String ip) {
        throw new UnsupportedOperationException("Pendiente: CU-05 (Bloque 2)");
    }

    /** Devuelve una atencion con sus diagnosticos, tratamientos y enmiendas (RN-06: solo de su hospital). */
    public AtencionDetalle detalle(Usuario usuario, Integer idAtencion, String ip) {
        throw new UnsupportedOperationException("Pendiente: CU-05 (Bloque 2)");
    }
}
