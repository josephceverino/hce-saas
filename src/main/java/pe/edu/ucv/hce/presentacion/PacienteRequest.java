package pe.edu.ucv.hce.presentacion;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** Datos que llegan desde el formulario de registro (RF-01). Plan v3, 1.3: filiacion completa. */
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
        @NotBlank(message = "Ingresa la direccion.") String direccion,
        @NotBlank(message = "Ingresa el lugar de nacimiento.") @Size(max = 100, message = "El lugar de nacimiento no puede pasar de 100 caracteres.") String lugarNacimiento,
        @NotBlank(message = "Ingresa el lugar de procedencia.") @Size(max = 100, message = "La procedencia no puede pasar de 100 caracteres.") String procedencia,
        @NotBlank(message = "Elige el estado civil.") String estadoCivil,
        @NotBlank(message = "Elige el grado de instruccion.") String gradoInstruccion,
        @NotBlank(message = "Ingresa la ocupacion.") @Size(max = 100, message = "La ocupacion no puede pasar de 100 caracteres.") String ocupacion,
        @Size(max = 60, message = "La religion no puede pasar de 60 caracteres.") String religion,
        @NotBlank(message = "Ingresa el nombre del contacto de emergencia.") @Size(max = 150, message = "El nombre del contacto no puede pasar de 150 caracteres.") String contactoEmergenciaNombre,
        @NotBlank(message = "Indica el parentesco del contacto de emergencia.") @Size(max = 40, message = "El parentesco no puede pasar de 40 caracteres.") String contactoEmergenciaParentesco,
        @NotBlank(message = "Ingresa el telefono del contacto de emergencia.")
        @Pattern(regexp = "[0-9 +]{6,20}", message = "El telefono del contacto de emergencia solo admite numeros.") String contactoEmergenciaTelefono) {
}
