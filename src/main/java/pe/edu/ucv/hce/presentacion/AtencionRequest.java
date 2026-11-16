package pe.edu.ucv.hce.presentacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Datos para crear una atencion (CU-05, RF-05). */
public record AtencionRequest(
        @NotNull(message = "Elige la historia clinica.") Integer idHistoriaClinica,
        @NotBlank(message = "Ingresa el motivo de consulta.") String motivo,
        @NotBlank(message = "Ingresa la anamnesis.") String anamnesis,
        @NotBlank(message = "Ingresa el examen fisico.") String examenFisico) {
}
