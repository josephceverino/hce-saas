package pe.edu.ucv.hce.presentacion;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;

/** Datos que llegan desde el formulario de registro. */
public record PacienteRequest(
        @NotBlank(message = "Elige el tipo de documento.") String tipoDocumento,
        @NotBlank(message = "Ingresa el numero de documento.") String nroDocumento,
        @NotBlank(message = "Ingresa los nombres.") String nombres,
        @NotBlank(message = "Ingresa los apellidos.") String apellidos,
        @NotNull(message = "Ingresa la fecha de nacimiento.")
        @Past(message = "La fecha de nacimiento debe ser anterior a hoy.") LocalDate fechaNacimiento,
        @NotBlank(message = "Elige el sexo.") String sexo,
        @NotBlank(message = "Ingresa el telefono.") String telefono,
        @NotBlank(message = "Ingresa el correo.") @Email(message = "El correo no es valido.") String correo,
        @NotBlank(message = "Ingresa la direccion.") String direccion) {
}
