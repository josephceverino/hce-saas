package pe.edu.ucv.hce.aplicacion;

import java.util.List;
import pe.edu.ucv.hce.dominio.Atencion;
import pe.edu.ucv.hce.dominio.Diagnostico;
import pe.edu.ucv.hce.dominio.Enmienda;
import pe.edu.ucv.hce.dominio.SolicitudExamen;
import pe.edu.ucv.hce.dominio.Tratamiento;

/** Una atencion con todo lo que cuelga de ella (CU-05, CU-06, CU-08). Se arma dentro del servicio. */
public record AtencionDetalle(Atencion atencion, List<Diagnostico> diagnosticos,
                              List<Tratamiento> tratamientos, List<Enmienda> enmiendas,
                              List<SolicitudExamen> examenes) { }
