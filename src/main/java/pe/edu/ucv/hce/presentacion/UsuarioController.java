package pe.edu.ucv.hce.presentacion;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.ucv.hce.aplicacion.UsuariosHospitalService;
import pe.edu.ucv.hce.aplicacion.UsuariosHospitalService.DatosUsuario;
import pe.edu.ucv.hce.dominio.Usuario;

/**
 * CU-17 Gestionar usuarios del hospital (RF-11, RF-12; RN-16, RN-14, RN-07). Solo el administrador del hospital.
 * Nunca recibe un hospital: sale de la sesion. Nunca devuelve hashes de clave.
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    public record UsuarioDto(Integer id, String usuario, String nombre, String documento, String correo, String rol,
                             String rolCodigo, String estado, boolean debeCambiarClave, LocalDateTime ultimoAcceso,
                             String colegiatura) { }

    public record AltaResponse(UsuarioDto usuario, String claveTemporal) { }

    private final UsuariosHospitalService servicio;
    private final SesionActual sesionActual;

    public UsuarioController(UsuariosHospitalService servicio, SesionActual sesionActual) {
        this.servicio = servicio;
        this.sesionActual = sesionActual;
    }

    private UsuarioDto dto(Usuario u) {
        String colegiatura = servicio.profesionalDe(u).map(p -> p.getColegiatura()).orElse(null);
        return new UsuarioDto(u.getId(), u.getUsuario(), u.getNombreCompleto(), u.getDocumento(), u.getCorreo(),
            u.getRol().getDescripcion(), u.getRol().getNombre(), u.getEstado(), u.isDebeCambiarClave(),
            u.getUltimoAcceso(), colegiatura);
    }

    @GetMapping
    public List<UsuarioDto> listar(HttpSession sesion) {
        return servicio.listar(sesionActual.requerir(sesion)).stream().map(this::dto).toList();
    }

    @PostMapping
    public ResponseEntity<AltaResponse> crear(@Valid @RequestBody UsuarioRequest d, HttpSession sesion,
                                              HttpServletRequest peticion) {
        var alta = servicio.crear(sesionActual.requerir(sesion), new DatosUsuario(d.nombres(), d.apellidos(),
            d.documento(), d.correo(), d.usuario(), d.rol(), d.colegiatura(), d.especialidad()), peticion.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED).body(new AltaResponse(dto(alta.usuario()), alta.claveTemporal()));
    }

    @PostMapping("/{id}/desactivacion")
    public UsuarioDto desactivar(@PathVariable Integer id, HttpSession sesion, HttpServletRequest peticion) {
        return dto(servicio.desactivar(sesionActual.requerir(sesion), id, peticion.getRemoteAddr()));
    }

    @PostMapping("/{id}/reactivacion")
    public UsuarioDto reactivar(@PathVariable Integer id, HttpSession sesion, HttpServletRequest peticion) {
        return dto(servicio.reactivar(sesionActual.requerir(sesion), id, peticion.getRemoteAddr()));
    }
}
