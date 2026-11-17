package pe.edu.ucv.hce.presentacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Datos para crear una atencion (CU-05, RF-05). Con idAdmision (desde la cola) la historia sale de la admision y los
 * campos ampliados (enfermedad actual, funciones biologicas, examen por sistemas) son opcionales.
 */
public record AtencionRequest(
        Integer idHistoriaClinica,
        Integer idAdmision,
        @NotBlank(message = "Ingresa el motivo de consulta.") String motivo,
        @NotBlank(message = "Ingresa la anamnesis.") String anamnesis,
        @NotBlank(message = "Ingresa el examen fisico.") String examenFisico,
        String enfermedadActual,
        String funcionesBiologicas,
        String examenSistemas) {
}
