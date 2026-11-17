package pe.edu.ucv.hce.aplicacion;

import pe.edu.ucv.hce.dominio.HistoriaClinica;

/**
 * Resultado de CU-03: la historia y si ya existia en el hospital (flujo alternativo, RN-01).
 * CU-19 (RN-15, RN-14): si se creo la cuenta del portal, su usuario y la clave temporal, que solo viaja aqui una vez.
 */
public record ResultadoRegistro(HistoriaClinica historia, boolean yaExistia, String usuarioPortal, String claveTemporal) {
    public ResultadoRegistro(HistoriaClinica historia, boolean yaExistia) {
        this(historia, yaExistia, null, null);
    }
}
