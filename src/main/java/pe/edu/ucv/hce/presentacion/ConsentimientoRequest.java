package pe.edu.ucv.hce.presentacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Consentimiento que el paciente otorga a un tercero (CU-11, RF-17; RN-09: el alcance declara la finalidad). */
public record ConsentimientoRequest(
        @NotBlank(message = "Indica a quién das el acceso.")
        @Size(max = 150, message = "El nombre no puede pasar de 150 caracteres.") String otorgadoA,
        @NotBlank(message = "Indica el alcance del acceso.")
        @Size(max = 200, message = "El alcance no puede pasar de 200 caracteres.") String alcance) {
}
