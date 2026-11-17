package pe.edu.ucv.hce.presentacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** CU-20: admitir al paciente del dia (RF-21). */
public record AdmisionRequest(
        @NotNull(message = "Elige la historia clinica del paciente.") Integer idHistoriaClinica,
        @NotBlank(message = "Elige el tipo de atencion.") String tipoAtencion) {
}
