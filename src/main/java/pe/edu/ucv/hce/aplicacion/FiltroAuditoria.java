package pe.edu.ucv.hce.aplicacion;

import java.time.LocalDate;

/** Filtros opcionales de la pantalla de auditoria. Un campo nulo o en blanco no filtra. */
public record FiltroAuditoria(String usuario, String accion, LocalDate desde, LocalDate hasta, String nroHc) { }
