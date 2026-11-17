package pe.edu.ucv.hce.presentacion;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** CU-17: datos del personal que crea el administrador del hospital (RF-11). */
public record UsuarioRequest(
        @NotBlank(message = "Ingresa los nombres.") @Size(max = 100, message = "Los nombres no pueden pasar de 100 caracteres.") String nombres,
        @NotBlank(message = "Ingresa los apellidos.") @Size(max = 100, message = "Los apellidos no pueden pasar de 100 caracteres.") String apellidos,
        @NotBlank(message = "Ingresa el documento de identidad.")
        @Pattern(regexp = "[0-9A-Za-z]{8,12}", message = "El documento debe tener entre 8 y 12 letras o números.") String documento,
        @NotBlank(message = "Ingresa el correo.") @Email(message = "El correo no es válido.") String correo,
        @NotBlank(message = "Ingresa el usuario.")
        @Pattern(regexp = "[a-zA-Z0-9._-]{4,50}", message = "El usuario debe tener entre 4 y 50 letras, números, puntos o guiones.") String usuario,
        @NotBlank(message = "Elige el rol.") String rol,
        @Size(max = 20, message = "La colegiatura no puede pasar de 20 caracteres.") String colegiatura,
        @Size(max = 100, message = "La especialidad no puede pasar de 100 caracteres.") String especialidad) {
}
