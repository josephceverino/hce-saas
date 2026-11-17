package pe.edu.ucv.hce.presentacion;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.ucv.hce.aplicacion.PlataformaService;
import pe.edu.ucv.hce.aplicacion.PlataformaService.DatosHospital;

/** CU-16 Dar de alta un hospital (RF-20; RN-16, RN-14, RN-07). La lista sale de GET /api/indicadores/hospitales. */
@RestController
@RequestMapping("/api/hospitales")
public class HospitalController {

    /** Constancia del alta: la clave temporal se entrega una sola vez. */
    public record AltaResponse(Integer idHospital, String hospital, String codigo, String plan, LocalDate vence,
                               String usuario, String nombreAdministrador, String claveTemporal) { }

    private final PlataformaService plataforma;
    private final SesionActual sesionActual;

    public HospitalController(PlataformaService plataforma, SesionActual sesionActual) {
        this.plataforma = plataforma;
        this.sesionActual = sesionActual;
    }

    @PostMapping
    public ResponseEntity<AltaResponse> alta(@Valid @RequestBody HospitalRequest d, HttpSession sesion,
                                             HttpServletRequest peticion) {
        var r = plataforma.altaHospital(sesionActual.requerir(sesion), new DatosHospital(d.nombre(), d.codigo(),
            d.region(), d.plan(), d.adminNombre(), d.adminDocumento(), d.adminCorreo(), d.adminUsuario()),
            peticion.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED).body(new AltaResponse(r.hospital().getId(),
            r.hospital().getNombre(), r.hospital().getCodigoIpress(), r.suscripcion().getPlan(),
            r.suscripcion().getFechaFin(), r.administrador().getUsuario(), r.administrador().getNombreCompleto(),
            r.claveTemporal()));
    }
}
