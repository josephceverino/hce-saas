package pe.edu.ucv.hce.presentacion;

import java.time.LocalDate;
import pe.edu.ucv.hce.aplicacion.ResultadoRegistro;

/**
 * Respuesta de CU-03 / CU-19: los datos de la historia (como HistoriaResumen) y, si se creo, la cuenta del portal
 * con su clave temporal, que se muestra una sola vez en la constancia (RF-27; RN-15, RN-14).
 */
public record RegistroPacienteResponse(Integer idHistoriaClinica, String nroHc, String tipoDocumento,
                                       String nroDocumento, String paciente, LocalDate fechaApertura,
                                       String establecimiento, String estado, CuentaPortal cuentaPortal) {

    public record CuentaPortal(String usuario, String claveTemporal) { }

    public static RegistroPacienteResponse de(ResultadoRegistro r) {
        HistoriaResumen h = HistoriaResumen.de(r.historia());
        CuentaPortal cuenta = r.claveTemporal() == null ? null : new CuentaPortal(r.usuarioPortal(), r.claveTemporal());
        return new RegistroPacienteResponse(h.idHistoriaClinica(), h.nroHc(), h.tipoDocumento(), h.nroDocumento(),
            h.paciente(), h.fechaApertura(), h.establecimiento(), h.estado(), cuenta);
    }
}
