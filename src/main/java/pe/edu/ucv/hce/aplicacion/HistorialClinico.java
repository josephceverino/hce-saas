package pe.edu.ucv.hce.aplicacion;

import java.util.List;
import pe.edu.ucv.hce.dominio.Alergia;
import pe.edu.ucv.hce.dominio.Antecedente;
import pe.edu.ucv.hce.dominio.HistoriaClinica;

/** Historia clinica completa para CU-06: cabecera, antecedentes, alergias y atenciones (mas reciente primero). */
public record HistorialClinico(HistoriaClinica historia, List<Antecedente> antecedentes,
                               List<Alergia> alergias, List<AtencionDetalle> atenciones) { }
