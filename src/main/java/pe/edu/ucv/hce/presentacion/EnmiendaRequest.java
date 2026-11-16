package pe.edu.ucv.hce.presentacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Enmienda en texto libre sobre una atencion cerrada (RN-03). */
public record EnmiendaRequest(
        @NotBlank(message = "Ingresa el motivo de la correccion.")
        @Size(max = 200, message = "El motivo no puede pasar de 200 caracteres.") String motivo,
        @NotBlank(message = "Ingresa el contenido corregido.") String contenidoCorregido) {
}
