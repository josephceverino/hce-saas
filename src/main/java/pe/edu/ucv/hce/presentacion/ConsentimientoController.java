package pe.edu.ucv.hce.presentacion;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.ucv.hce.aplicacion.ConsentimientoService;
import pe.edu.ucv.hce.dominio.Usuario;

/**
 * CU-11: consentimiento de acceso del paciente (RF-17; RN-04, RN-06, RN-07, RN-09).
 * Contrato: docs/contratos/consentimiento.md. No recibe ningun id de paciente: sale siempre de la cuenta en sesion.
 */
@RestController
@RequestMapping("/api/consentimientos")
public class ConsentimientoController {

    private final ConsentimientoService servicio;
    private final SesionActual sesionActual;

    public ConsentimientoController(ConsentimientoService servicio, SesionActual sesionActual) {
        this.servicio = servicio;
        this.sesionActual = sesionActual;
    }

    @GetMapping
    public List<ConsentimientoResponse> listar(HttpSession sesion, HttpServletRequest peticion) {
        Usuario usuario = sesionActual.requerir(sesion);
        return servicio.listar(usuario, peticion.getRemoteAddr()).stream().map(ConsentimientoResponse::de).toList();
    }

    @PostMapping
    public ResponseEntity<ConsentimientoResponse> otorgar(@Valid @RequestBody ConsentimientoRequest datos,
            HttpSession sesion, HttpServletRequest peticion) {
        Usuario usuario = sesionActual.requerir(sesion);
        var c = servicio.otorgar(usuario, datos.otorgadoA(), datos.alcance(), peticion.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED).body(ConsentimientoResponse.de(c));
    }

    @PostMapping("/{id}/revocacion")
    public ConsentimientoResponse revocar(@PathVariable Integer id, HttpSession sesion, HttpServletRequest peticion) {
        Usuario usuario = sesionActual.requerir(sesion);
        return ConsentimientoResponse.de(servicio.revocar(usuario, id, peticion.getRemoteAddr()));
    }
}
