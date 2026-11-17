package pe.edu.ucv.hce.presentacion;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.ucv.hce.aplicacion.ExamenService;
import pe.edu.ucv.hce.dominio.SolicitudExamen;

/**
 * CU-22 y CU-23 (RF-25, RF-09; RN-02, RN-03, RN-06, RN-07, RN-18). El controlador solo traduce HTTP:
 * permisos, hospital y estados los resuelve ExamenService.
 */
@RestController
public class ExamenController {

    public record SolicitudRequest(@NotBlank(message = "Ingresa el tipo de examen.") String tipoExamen,
                                   String indicaciones) { }

    public record ResultadoRequest(@NotBlank(message = "Ingresa el resultado del examen.") String resultado) { }

    /** Fila de la bandeja de apoyo al diagnostico: incluye al paciente (la historia a la que pertenece el examen). */
    public record ExamenBandejaDto(HistoriaDetalleResponse.ExamenDto examen, String paciente, String nroHc,
                                   String documento) { }

    private final ExamenService servicio;
    private final SesionActual sesionActual;

    public ExamenController(ExamenService servicio, SesionActual sesionActual) {
        this.servicio = servicio;
        this.sesionActual = sesionActual;
    }

    @PostMapping("/api/atenciones/{idAtencion}/examenes")
    public ResponseEntity<HistoriaDetalleResponse.ExamenDto> solicitar(@PathVariable Integer idAtencion,
            @Valid @RequestBody SolicitudRequest d, HttpSession sesion, HttpServletRequest peticion) {
        var s = servicio.solicitar(sesionActual.requerir(sesion), idAtencion, d.tipoExamen(), d.indicaciones(),
            peticion.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED).body(HistoriaDetalleResponse.examen(s));
    }

    @GetMapping("/api/examenes")
    public List<ExamenBandejaDto> lista(@RequestParam(defaultValue = SolicitudExamen.SOLICITADO) String estado,
                                        HttpSession sesion) {
        return servicio.delHospital(sesionActual.requerir(sesion), estado).stream().map(s -> {
            var h = s.getAtencion().getHistoriaClinica();
            var p = h.getPaciente();
            return new ExamenBandejaDto(HistoriaDetalleResponse.examen(s), p.getApellidos() + ", " + p.getNombres(),
                h.getNroHc(), p.getTipoDocumento() + " " + p.getNroDocumento());
        }).toList();
    }

    @PostMapping("/api/examenes/{idSolicitud}/resultado")
    public HistoriaDetalleResponse.ExamenDto resultado(@PathVariable Integer idSolicitud,
            @Valid @RequestBody ResultadoRequest d, HttpSession sesion, HttpServletRequest peticion) {
        return HistoriaDetalleResponse.examen(servicio.registrarResultado(sesionActual.requerir(sesion), idSolicitud,
            d.resultado(), peticion.getRemoteAddr()));
    }
}
