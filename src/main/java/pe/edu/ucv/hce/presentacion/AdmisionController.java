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
import pe.edu.ucv.hce.aplicacion.AdmisionService;

/**
 * CU-20 y RF-22: admision del dia y cola por rol (RN-13, RN-06, RN-07). El controlador solo traduce HTTP:
 * quien puede mover cada estado y en que orden lo deciden AdmisionService y el dominio.
 */
@RestController
@RequestMapping("/api/admisiones")
public class AdmisionController {

    private final AdmisionService servicio;
    private final SesionActual sesionActual;

    public AdmisionController(AdmisionService servicio, SesionActual sesionActual) {
        this.servicio = servicio;
        this.sesionActual = sesionActual;
    }

    @PostMapping
    public ResponseEntity<AdmisionDto> admitir(@Valid @RequestBody AdmisionRequest d, HttpSession sesion,
                                               HttpServletRequest peticion) {
        var a = servicio.admitir(sesionActual.requerir(sesion), d.idHistoriaClinica(), d.tipoAtencion(),
            peticion.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED).body(AdmisionDto.de(a));
    }

    /** RF-22: la cola del dia del rol (admision ve todo; enfermeria y medico, su parte). */
    @GetMapping("/cola")
    public List<AdmisionDto> cola(HttpSession sesion) {
        return servicio.cola(sesionActual.requerir(sesion)).stream().map(AdmisionDto::de).toList();
    }

    @GetMapping("/{id}")
    public AdmisionDto ver(@PathVariable Integer id, HttpSession sesion) {
        return AdmisionDto.de(servicio.delHospital(sesionActual.requerir(sesion), id));
    }

    @PostMapping("/{id}/cancelacion")
    public AdmisionDto cancelar(@PathVariable Integer id, HttpSession sesion, HttpServletRequest peticion) {
        return AdmisionDto.de(servicio.cancelar(sesionActual.requerir(sesion), id, peticion.getRemoteAddr()));
    }

    @PostMapping("/{id}/toma")
    public AdmisionDto tomar(@PathVariable Integer id, HttpSession sesion, HttpServletRequest peticion) {
        return AdmisionDto.de(servicio.tomarEnEnfermeria(sesionActual.requerir(sesion), id, peticion.getRemoteAddr()));
    }

    @PostMapping("/{id}/listo")
    public AdmisionDto listo(@PathVariable Integer id, HttpSession sesion, HttpServletRequest peticion) {
        return AdmisionDto.de(servicio.marcarListo(sesionActual.requerir(sesion), id, peticion.getRemoteAddr()));
    }
}
