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
import pe.edu.ucv.hce.aplicacion.TratamientoService;
import pe.edu.ucv.hce.dominio.Usuario;

/**
 * CU-06, CU-05 y CU-08 (RF-05, RF-06, RF-08; RN-02, RN-03, RN-06, RN-07, RN-10). Contrato: docs/contratos/atencion_historia.md.
 * El controlador solo traduce HTTP: permisos, hospital y reglas los resuelven los servicios.
 */
@RestController
@RequestMapping("/api")
public class AtencionController {

    private final HistorialService historial;
    private final AtencionService atencion;
    private final TratamientoService tratamiento;
    private final SesionActual sesionActual;

    public AtencionController(HistorialService historial, AtencionService atencion,
                              TratamientoService tratamiento, SesionActual sesionActual) {
        this.historial = historial;
        this.atencion = atencion;
        this.tratamiento = tratamiento;
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
        if (datos.idAdmision() == null && datos.idHistoriaClinica() == null) {
            throw new IllegalArgumentException("Elige la historia clínica o la admisión.");
        }
        var detalle = datos.idAdmision() != null
            ? atencion.crearDesdeAdmision(usuario, datos.idAdmision(), datos.motivo(), datos.anamnesis(),
                datos.examenFisico(), new AtencionService.Ampliacion(datos.enfermedadActual(),
                    datos.funcionesBiologicas(), datos.examenSistemas()), peticion.getRemoteAddr())
            : atencion.crear(usuario, datos.idHistoriaClinica(), datos.motivo(), datos.anamnesis(),
                datos.examenFisico(), peticion.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED).body(HistoriaDetalleResponse.atencion(detalle));
    }

    /** CU-05 ampliado: la atencion de una admision de la cola (404 si todavia no se inicio). */
    @GetMapping("/admisiones/{idAdmision}/atencion")
    public java.util.Map<String, Integer> deAdmision(@PathVariable Integer idAdmision, HttpSession sesion) {
        Usuario usuario = sesionActual.requerir(sesion);
        Integer id = atencion.idDeAdmision(usuario, idAdmision).orElseThrow(() ->
            new pe.edu.ucv.hce.dominio.RecursoNoEncontradoException("Esa admisión todavía no tiene una atención iniciada."));
        return java.util.Map.of("idAtencion", id);
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
        var d = atencion.agregarDiagnostico(usuario, idAtencion, datos.codigoCie10(), datos.tipo(), datos.certeza(),
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

    /** CU-08 (RF-08; RN-02, RN-03, RN-06, RN-07, RN-11): tratamiento sobre una atencion abierta propia. */
    @PostMapping("/atenciones/{idAtencion}/tratamientos")
    public ResponseEntity<HistoriaDetalleResponse.TratamientoDto> tratamiento(@PathVariable Integer idAtencion,
            @Valid @RequestBody TratamientoRequest datos, HttpSession sesion, HttpServletRequest peticion) {
        Usuario usuario = sesionActual.requerir(sesion);
        var t = tratamiento.agregar(usuario, idAtencion, datos.indicacion(), datos.medicamento(), datos.dosis(),
            datos.duracion(), peticion.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED).body(HistoriaDetalleResponse.tratamiento(t));
    }
}
