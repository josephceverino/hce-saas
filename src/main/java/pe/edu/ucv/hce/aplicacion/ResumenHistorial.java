package pe.edu.ucv.hce.aplicacion;

import java.time.LocalDateTime;
import java.util.List;
import pe.edu.ucv.hce.dominio.Alergia;
import pe.edu.ucv.hce.dominio.Antecedente;
import pe.edu.ucv.hce.dominio.Diagnostico;
import pe.edu.ucv.hce.dominio.HistoriaClinica;
import pe.edu.ucv.hce.dominio.SolicitudExamen;

/**
 * Resumen de la historia para el paciente (CU-10, RF-16). Solo cuenta atenciones cerradas.
 * diagnosticos: los distintos (por codigo CIE-10) de esas atenciones.
 * examenes: los de esas atenciones que ya tienen resultado (R4.2).
 */
public record ResumenHistorial(HistoriaClinica historia, List<Alergia> alergias, List<Antecedente> antecedentes,
                               int totalAtenciones, LocalDateTime ultimaAtencion,
                               List<Diagnostico> diagnosticos, List<SolicitudExamen> examenes,
                               LocalDateTime generadoEn) { }
