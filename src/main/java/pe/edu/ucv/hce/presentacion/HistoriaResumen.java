package pe.edu.ucv.hce.presentacion;

import java.time.LocalDate;
import pe.edu.ucv.hce.dominio.HistoriaClinica;

/** Lo que se muestra en la tabla de historias abiertas. */
public record HistoriaResumen(Integer idHistoriaClinica, String nroHc, String tipoDocumento,
                              String nroDocumento, String paciente, LocalDate fechaApertura,
                              String establecimiento, String estado) {

    public static HistoriaResumen de(HistoriaClinica h) {
        return new HistoriaResumen(
            h.getId(), h.getNroHc(),
            h.getPaciente().getTipoDocumento(), h.getPaciente().getNroDocumento(),
            h.getPaciente().getApellidos() + ", " + h.getPaciente().getNombres(),
            h.getFechaApertura(), h.getEstablecimiento().getNombre(), h.getEstado());
    }
}
