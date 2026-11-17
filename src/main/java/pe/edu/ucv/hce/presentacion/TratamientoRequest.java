package pe.edu.ucv.hce.presentacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Tratamiento e indicaciones de una atencion abierta (CU-08, RF-08). */
public record TratamientoRequest(
    @NotBlank(message = "Ingresa la indicacion.") String indicacion,
    @NotBlank(message = "Ingresa el medicamento.")
    @Size(max = 150, message = "El medicamento no puede pasar de 150 caracteres.") String medicamento,
    @NotBlank(message = "Ingresa la dosis.")
    @Size(max = 100, message = "La dosis no puede pasar de 100 caracteres.") String dosis,
    @NotBlank(message = "Ingresa la duracion.")
    @Size(max = 100, message = "La duracion no puede pasar de 100 caracteres.") String duracion) {
}