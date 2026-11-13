package pe.edu.ucv.hce.aplicacion;

import pe.edu.ucv.hce.dominio.HistoriaClinica;

/** Resultado de CU-03: la historia y si ya existia en el hospital (flujo alternativo, RN-01). */
public record ResultadoRegistro(HistoriaClinica historia, boolean yaExistia) { }
