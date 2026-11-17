package pe.edu.ucv.hce.presentacion;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.ucv.hce.aplicacion.RestablecimientoClaveService;
import pe.edu.ucv.hce.aplicacion.RestablecimientoClaveService.ClaveRestablecida;

/** CU-26 Restablecer clave (RN-14, RN-16, RN-06, RN-07). Nunca devuelve hashes; la clave temporal se entrega una vez. */
@RestController
@RequestMapping("/api/restablecimientos")
public class RestablecimientoClaveController {

    public record RestablecimientoResponse(Integer idUsuario, String usuario, String nombre, String claveTemporal) { }

    private final RestablecimientoClaveService servicio;
    private final SesionActual sesionActual;

    public RestablecimientoClaveController(RestablecimientoClaveService servicio, SesionActual sesionActual) {
        this.servicio = servicio;
        this.sesionActual = sesionActual;
    }

    private static RestablecimientoResponse respuesta(ClaveRestablecida r) {
        return new RestablecimientoResponse(r.cuenta().getId(), r.cuenta().getUsuario(),
            r.cuenta().getNombreCompleto(), r.claveTemporal());
    }

    @PostMapping("/personal/{idUsuario}")
    public RestablecimientoResponse personal(@PathVariable Integer idUsuario, HttpSession sesion,
                                             HttpServletRequest peticion) {
        return respuesta(servicio.restablecerPersonal(sesionActual.requerir(sesion), idUsuario, peticion.getRemoteAddr()));
    }

    @PostMapping("/hospitales/{idHospital}")
    public RestablecimientoResponse administrador(@PathVariable Integer idHospital, HttpSession sesion,
                                                  HttpServletRequest peticion) {
        return respuesta(servicio.restablecerAdministradorDeHospital(sesionActual.requerir(sesion), idHospital,
            peticion.getRemoteAddr()));
    }

    @PostMapping("/pacientes/{idHistoria}")
    public RestablecimientoResponse paciente(@PathVariable Integer idHistoria, HttpSession sesion,
                                             HttpServletRequest peticion) {
        return respuesta(servicio.restablecerPaciente(sesionActual.requerir(sesion), idHistoria, peticion.getRemoteAddr()));
    }
}
