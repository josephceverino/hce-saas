package pe.edu.ucv.hce.presentacion;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.ucv.hce.aplicacion.AtencionService;
import pe.edu.ucv.hce.aplicacion.HistorialService;
import pe.edu.ucv.hce.dominio.Usuario;

/**
 * CU-06 y CU-05 (RF-05, RF-06; RN-02, RN-03, RN-06, RN-07, RN-10). Contrato: docs/contratos/atencion_historia.md.
 * El controlador solo traduce HTTP: permisos, hospital y reglas los resuelven los servicios.
 */
@RestController
@RequestMapping("/api")
public class AtencionController {

    private final HistorialService historial;
    private final AtencionService atencion;
    private final SesionActual sesionActual;

    public AtencionController(HistorialService historial, AtencionService atencion, SesionActual sesionActual) {
        this.historial = historial;
        this.atencion = atencion;
        this.sesionActual = sesionActual;
    }

    @GetMapping("/historias/{idHistoria}")
    public HistoriaDetalleResponse historia(@PathVariable Integer idHistoria, HttpSession sesion,
                                            HttpServletRequest peticion) {
        Usuario usuario = sesionActual.requerir(sesion);
        return HistoriaDetalleResponse.de(historial.consultar(usuario, idHistoria, peticion.getRemoteAddr()));
    }

    @PostMapping("/atenciones")
    public ResponseEntity<HistoriaDetalleResponse.AtencionDto> crear(@Valid @RequestBody AtencionRequest datos,
            HttpSession sesion, HttpServletRequest peticion) {
        Usuario usuario = sesionActual.requerir(sesion);
        var detalle = atencion.crear(usuario, datos.idHistoriaClinica(), datos.motivo(), datos.anamnesis(),
            datos.examenFisico(), peticion.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED).body(HistoriaDetalleResponse.atencion(detalle));
    }

    @GetMapping("/atenciones/{idAtencion}")
    public HistoriaDetalleResponse.AtencionDto ver(@PathVariable Integer idAtencion, HttpSession sesion,
                                                   HttpServletRequest peticion) {
        Usuario usuario = sesionActual.requerir(sesion);
        return HistoriaDetalleResponse.atencion(atencion.detalle(usuario, idAtencion, peticion.getRemoteAddr()));
    }

    @PostMapping("/atenciones/{idAtencion}/diagnosticos")
    public ResponseEntity<HistoriaDetalleResponse.DiagnosticoDto> diagnostico(@PathVariable Integer idAtencion,
            @Valid @RequestBody DiagnosticoRequest datos, HttpSession sesion, HttpServletRequest peticion) {
        Usuario usuario = sesionActual.requerir(sesion);
        var d = atencion.agregarDiagnostico(usuario, idAtencion, datos.codigoCie10(), datos.tipo(),
            peticion.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED).body(HistoriaDetalleResponse.diagnostico(d));
    }

    @PostMapping("/atenciones/{idAtencion}/cierre")
    public HistoriaDetalleResponse.AtencionDto cerrar(@PathVariable Integer idAtencion, HttpSession sesion,
                                                      HttpServletRequest peticion) {
        Usuario usuario = sesionActual.requerir(sesion);
        return HistoriaDetalleResponse.atencion(atencion.cerrar(usuario, idAtencion, peticion.getRemoteAddr()));
    }

    @PostMapping("/atenciones/{idAtencion}/enmiendas")
    public ResponseEntity<HistoriaDetalleResponse.EnmiendaDto> enmendar(@PathVariable Integer idAtencion,
            @Valid @RequestBody EnmiendaRequest datos, HttpSession sesion, HttpServletRequest peticion) {
        Usuario usuario = sesionActual.requerir(sesion);
        var e = atencion.enmendar(usuario, idAtencion, datos.motivo(), datos.contenidoCorregido(),
            peticion.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED).body(HistoriaDetalleResponse.enmienda(e));
    }
}
