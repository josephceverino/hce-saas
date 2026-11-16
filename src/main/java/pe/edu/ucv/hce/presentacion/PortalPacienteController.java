package pe.edu.ucv.hce.presentacion;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.ucv.hce.aplicacion.PortalPacienteService;
import pe.edu.ucv.hce.dominio.Usuario;

/**
 * CU-10: portal del paciente, solo lectura (RF-14 a RF-16; RN-04). Contrato: docs/contratos/portal_paciente.md.
 * No recibe ningun id: la historia sale siempre de la cuenta en sesion. Solo GET.
 */
@RestController
@RequestMapping("/api/mi-historial")
public class PortalPacienteController {

    private final PortalPacienteService servicio;
    private final SesionActual sesionActual;

    public PortalPacienteController(PortalPacienteService servicio, SesionActual sesionActual) {
        this.servicio = servicio;
        this.sesionActual = sesionActual;
    }

    @GetMapping
    public HistoriaDetalleResponse miHistorial(HttpSession sesion, HttpServletRequest peticion) {
        Usuario usuario = sesionActual.requerir(sesion);
        return HistoriaDetalleResponse.de(servicio.miHistorial(usuario, peticion.getRemoteAddr()));
    }

    @GetMapping("/resumen")
    public ResumenHistorialResponse resumen(HttpSession sesion, HttpServletRequest peticion) {
        Usuario usuario = sesionActual.requerir(sesion);
        return ResumenHistorialResponse.de(servicio.miResumen(usuario, peticion.getRemoteAddr()));
    }
}
