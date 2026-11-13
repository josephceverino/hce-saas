package pe.edu.ucv.hce.presentacion;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.ucv.hce.aplicacion.RegistroPacienteService;
import pe.edu.ucv.hce.aplicacion.ResultadoRegistro;
import pe.edu.ucv.hce.dominio.NoAutenticadoException;
import pe.edu.ucv.hce.dominio.Paciente;
import pe.edu.ucv.hce.dominio.Usuario;

@RestController
@RequestMapping("/api")
public class PacienteController {

    private final RegistroPacienteService servicio;
    private final SesionActual sesionActual;

    public PacienteController(RegistroPacienteService servicio, SesionActual sesionActual) {
        this.servicio = servicio;
        this.sesionActual = sesionActual;
    }

    /** CU-03: 201 si abre la historia; 200 si ya existia en el hospital (RN-01); 409 si esta en otro (DEC-02). */
    @PostMapping("/pacientes")
    public ResponseEntity<HistoriaResumen> registrar(@Valid @RequestBody PacienteRequest datos, HttpSession sesion) {
        Usuario usuario = hospitalDe(sesion);
        Paciente paciente = new Paciente(
            datos.tipoDocumento().trim(), datos.nroDocumento().trim(), datos.nombres().trim(),
            datos.apellidos().trim(), datos.fechaNacimiento(), datos.sexo(), datos.telefono().trim(),
            datos.correo().trim(), datos.direccion().trim());
        ResultadoRegistro r = servicio.registrarPacienteYAbrirHistoria(paciente, usuario.getEstablecimiento());
        return ResponseEntity.status(r.yaExistia() ? HttpStatus.OK : HttpStatus.CREATED)
            .body(HistoriaResumen.de(r.historia()));
    }

    /** RN-06: devuelve solo las historias del hospital del usuario. */
    @GetMapping("/historias")
    public List<HistoriaResumen> listar(HttpSession sesion) {
        Usuario usuario = hospitalDe(sesion);
        return servicio.listarHistorias(usuario.getEstablecimiento().getId())
            .stream().map(HistoriaResumen::de).toList();
    }

    private Usuario hospitalDe(HttpSession sesion) {
        Usuario usuario = sesionActual.requerir(sesion);
        if (usuario.getEstablecimiento() == null) {
            throw new NoAutenticadoException("Tu usuario no pertenece a un hospital.");
        }
        return usuario;
    }
}
