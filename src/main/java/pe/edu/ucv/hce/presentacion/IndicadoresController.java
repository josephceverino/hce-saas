package pe.edu.ucv.hce.presentacion;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.ucv.hce.aplicacion.Indicadores;
import pe.edu.ucv.hce.aplicacion.IndicadoresService;

/**
 * Cifras reales para el Inicio por rol, el tablero del admin del hospital y el panel de plataforma
 * (RF-19, RF-20; DEC-10, DEC-11). Solo GET. Contrato: docs/contratos/auditoria_indicadores.md.
 */
@RestController
@RequestMapping("/api/indicadores")
public class IndicadoresController {

    private final IndicadoresService servicio;
    private final SesionActual sesionActual;

    public IndicadoresController(IndicadoresService servicio, SesionActual sesionActual) {
        this.servicio = servicio;
        this.sesionActual = sesionActual;
    }

    @GetMapping("/inicio")
    public Indicadores.Inicio inicio(HttpSession sesion) {
        return servicio.inicio(sesionActual.requerir(sesion));
    }

    @GetMapping("/tablero")
    public Indicadores.Tablero tablero(HttpSession sesion) {
        return servicio.tablero(sesionActual.requerir(sesion));
    }

    @GetMapping("/hospitales")
    public List<Indicadores.ResumenHospital> hospitales(HttpSession sesion) {
        return servicio.hospitales(sesionActual.requerir(sesion));
    }
}
