package pe.edu.ucv.hce.aplicacion;

import java.util.List;
import pe.edu.ucv.hce.dominio.RegistroAuditoria;

/** Una pagina de registros de auditoria (del mas reciente al mas antiguo). pagina empieza en 0. */
public record PaginaAuditoria(List<RegistroAuditoria> registros, int pagina, int tamano, long total,
                              int totalPaginas) { }
