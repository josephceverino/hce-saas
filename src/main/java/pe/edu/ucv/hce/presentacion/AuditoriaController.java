package pe.edu.ucv.hce.presentacion;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.ucv.hce.aplicacion.AuditoriaService;
import pe.edu.ucv.hce.aplicacion.FiltroAuditoria;

/**
 * Auditoria para el Administrador del hospital (RF-13, RN-07, RNF-08). Solo GET: un registro de auditoria
 * no se modifica ni se borra. Contrato: docs/contratos/auditoria_indicadores.md.
 */
@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

    private final AuditoriaService servicio;
    private final SesionActual sesionActual;

    public AuditoriaController(AuditoriaService servicio, SesionActual sesionActual) {
        this.servicio = servicio;
        this.sesionActual = sesionActual;
    }

    @GetMapping
    public AuditoriaResponse listar(
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) String accion,
            @RequestParam(required = false) String nroHc,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano,
            HttpSession sesion) {
        var solicitante = sesionActual.requerir(sesion);
        return AuditoriaResponse.de(servicio.consultar(solicitante,
            new FiltroAuditoria(usuario, accion, desde, hasta, nroHc), pagina, tamano));
    }

    /** Acciones que existen en el hospital, para el filtro. */
    @GetMapping("/acciones")
    public List<String> acciones(HttpSession sesion) {
        return servicio.accionesDelHospital(sesionActual.requerir(sesion));
    }
}
