package pe.edu.ucv.hce.presentacion;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** CU-16: datos del hospital y de su administrador (RF-20). */
public record HospitalRequest(
        @NotBlank(message = "Ingresa el nombre del hospital.")
        @Size(max = 150, message = "El nombre no puede pasar de 150 caracteres.") String nombre,
        @NotBlank(message = "Ingresa el código del establecimiento.")
        @Pattern(regexp = "[0-9]{5,10}", message = "El código del establecimiento debe tener entre 5 y 10 dígitos.") String codigo,
        @NotBlank(message = "Ingresa la región.")
        @Size(max = 50, message = "La región no puede pasar de 50 caracteres.") String region,
        @NotBlank(message = "Elige el plan de suscripción.") String plan,
        @NotBlank(message = "Ingresa el nombre del administrador.")
        @Size(max = 150, message = "El nombre no puede pasar de 150 caracteres.") String adminNombre,
        @NotBlank(message = "Ingresa el documento del administrador.")
        @Pattern(regexp = "[0-9A-Za-z]{8,12}", message = "El documento debe tener entre 8 y 12 letras o números.") String adminDocumento,
        @NotBlank(message = "Ingresa el correo del administrador.")
        @Email(message = "El correo del administrador no es válido.") String adminCorreo,
        @NotBlank(message = "Ingresa el usuario del administrador.")
        @Pattern(regexp = "[a-zA-Z0-9._-]{4,50}", message = "El usuario debe tener entre 4 y 50 letras, números, puntos o guiones.") String adminUsuario) {
}
