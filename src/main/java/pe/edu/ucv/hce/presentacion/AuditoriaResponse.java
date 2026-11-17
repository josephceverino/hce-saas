package pe.edu.ucv.hce.presentacion;

import java.time.LocalDateTime;
import java.util.List;
import pe.edu.ucv.hce.aplicacion.PaginaAuditoria;
import pe.edu.ucv.hce.dominio.RegistroAuditoria;

/** Respuesta de GET /api/auditoria (RF-13, RN-07). Contrato: docs/contratos/auditoria_indicadores.md. */
public record AuditoriaResponse(List<RegistroDto> registros, int pagina, int tamano, long total, int totalPaginas) {

    /** nroHc es null cuando la accion no toca una historia (por ejemplo, iniciar sesion). */
    public record RegistroDto(Integer id, LocalDateTime fechaHora, String usuario, String rol, String accion,
                              String entidadAfectada, Integer idEntidadAfectada, String nroHc, String ip) { }

    public static AuditoriaResponse de(PaginaAuditoria p) {
        return new AuditoriaResponse(p.registros().stream().map(AuditoriaResponse::registro).toList(),
            p.pagina(), p.tamano(), p.total(), p.totalPaginas());
    }

    private static RegistroDto registro(RegistroAuditoria r) {
        return new RegistroDto(r.getId(), r.getFechaHora(), r.getUsuario().getUsuario(),
            r.getUsuario().getRol().getDescripcion(), r.getAccion(), r.getEntidadAfectada(),
            r.getIdEntidadAfectada(), r.getHistoriaClinica() == null ? null : r.getHistoriaClinica().getNroHc(),
            r.getIp());
    }
}
