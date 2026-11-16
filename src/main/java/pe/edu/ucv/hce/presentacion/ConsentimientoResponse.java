package pe.edu.ucv.hce.presentacion;

import java.time.LocalDateTime;
import pe.edu.ucv.hce.dominio.ConsentimientoAcceso;

/** Un consentimiento tal como lo ve el paciente (CU-11). activo = true si estado es ACTIVO. */
public record ConsentimientoResponse(Integer id, String otorgadoA, String alcance, LocalDateTime fechaOtorgado,
                                     LocalDateTime fechaRevocado, String estado, boolean activo) {

    public static ConsentimientoResponse de(ConsentimientoAcceso c) {
        return new ConsentimientoResponse(c.getId(), c.getOtorgadoA(), c.getAlcance(), c.getFechaOtorgado(),
            c.getFechaRevocado(), c.getEstado(), c.estaActivo());
    }
}
