package pe.edu.ucv.hce.presentacion;

import jakarta.validation.constraints.NotBlank;

/** CU-18: cambio de clave (RF-28; RN-14, RN-12). */
public record CambioClaveRequest(
        @NotBlank(message = "Ingresa tu clave actual.") String claveActual,
        @NotBlank(message = "Ingresa la nueva clave.") String claveNueva) {
}
