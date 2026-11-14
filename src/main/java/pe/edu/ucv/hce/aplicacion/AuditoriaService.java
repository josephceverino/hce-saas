package pe.edu.ucv.hce.aplicacion;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ucv.hce.dominio.HistoriaClinica;
import pe.edu.ucv.hce.dominio.RegistroAuditoria;
import pe.edu.ucv.hce.dominio.Usuario;
import pe.edu.ucv.hce.persistencia.RegistroAuditoriaRepository;

/**
 * Auditoria transversal minima (DD-04, DEC-06; RN-07, RNF-08, RF-13).
 * Se invoca desde CU-03 (y en v3 desde CU-05, CU-06 y CU-08). En v5 se completa (pantalla, filtros, cobertura total).
 * Corre en la misma transaccion del caso de uso: si la operacion falla, tampoco queda el registro.
 */
@Service
public class AuditoriaService {

    public static final String IP_DESCONOCIDA = "desconocida";

    private final RegistroAuditoriaRepository registros;

    public AuditoriaService(RegistroAuditoriaRepository registros) {
        this.registros = registros;
    }

    @Transactional
    public RegistroAuditoria registrar(Usuario usuario, String accion, String entidadAfectada,
                                       Integer idEntidadAfectada, HistoriaClinica historia, String ip) {
        String origen = (ip == null || ip.isBlank()) ? IP_DESCONOCIDA : ip;
        return registros.save(new RegistroAuditoria(usuario, historia, LocalDateTime.now(),
            accion, entidadAfectada, idEntidadAfectada, origen));
    }
}
