package pe.edu.ucv.hce.presentacion;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Ingresa tu usuario.") String usuario,
        @NotBlank(message = "Ingresa tu clave.") String clave) {
}
