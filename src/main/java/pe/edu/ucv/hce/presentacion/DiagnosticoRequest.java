package pe.edu.ucv.hce.presentacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Diagnostico a agregar a una atencion abierta (CU-05; RN-10). */
public record DiagnosticoRequest(
        @NotBlank(message = "Elige el diagnostico CIE-10.") String codigoCie10,
        @NotBlank(message = "Indica si el diagnostico es principal o secundario.")
        @Pattern(regexp = "PRINCIPAL|SECUNDARIO", message = "El tipo debe ser PRINCIPAL o SECUNDARIO.") String tipo) {
}
