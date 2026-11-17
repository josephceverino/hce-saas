package pe.edu.ucv.hce.presentacion;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.ucv.hce.aplicacion.EnfermeriaService;
import pe.edu.ucv.hce.aplicacion.EnfermeriaService.DatosSignos;
import pe.edu.ucv.hce.dominio.SignosVitales;

/** CU-21: signos vitales de la admision (RF-23; RN-17) y antecedentes y alergias por tipo (RF-07). */
@RestController
public class EnfermeriaController {

    public record SignosRequest(
            @NotNull(message = "Ingresa la presión arterial sistólica.") Integer presionSistolica,
            @NotNull(message = "Ingresa la presión arterial diastólica.") Integer presionDiastolica,
            @NotNull(message = "Ingresa la frecuencia cardíaca.") Integer frecuenciaCardiaca,
            @NotNull(message = "Ingresa la frecuencia respiratoria.") Integer frecuenciaRespiratoria,
            @NotNull(message = "Ingresa la temperatura.") BigDecimal temperatura,
            @NotNull(message = "Ingresa la saturación de oxígeno.") Integer saturacionOxigeno) { }

    public record SignosDto(Integer id, Integer idAdmision, Integer presionSistolica, Integer presionDiastolica,
                            Integer frecuenciaCardiaca, Integer frecuenciaRespiratoria, BigDecimal temperatura,
                            Integer saturacionOxigeno, LocalDateTime fechaHora, String registradoPor) {
        static SignosDto de(SignosVitales s) {
            String quien = s.getUsuario().getNombreCompleto() != null ? s.getUsuario().getNombreCompleto() : s.getUsuario().getUsuario();
            return new SignosDto(s.getId(), s.getAdmision().getId(), s.getPresionSistolica(), s.getPresionDiastolica(),
                s.getFrecuenciaCardiaca(), s.getFrecuenciaRespiratoria(), s.getTemperatura(), s.getSaturacionOxigeno(),
                s.getFechaHora(), quien);
        }
    }

    public record AntecedenteRequest(
            @NotBlank(message = "Elige el tipo de antecedente.") String tipo,
            @NotBlank(message = "Describe el antecedente.") @Size(max = 500, message = "La descripción no puede pasar de 500 caracteres.") String descripcion) { }

    public record AlergiaRequest(
            @NotBlank(message = "Indica a qué es alérgico.") @Size(max = 100, message = "El agente no puede pasar de 100 caracteres.") String agente,
            @NotBlank(message = "Indica la reacción.") @Size(max = 200, message = "La reacción no puede pasar de 200 caracteres.") String reaccion,
            @NotBlank(message = "Elige la gravedad.") String gravedad) { }

    public record AntecedenteDto(Integer id, String tipo, String descripcion, LocalDate fechaRegistro) { }

    public record AlergiaDto(Integer id, String agente, String reaccion, String gravedad) { }

    private final EnfermeriaService servicio;
    private final SesionActual sesionActual;

    public EnfermeriaController(EnfermeriaService servicio, SesionActual sesionActual) {
        this.servicio = servicio;
        this.sesionActual = sesionActual;
    }

    @PostMapping("/api/admisiones/{id}/signos-vitales")
    public ResponseEntity<SignosDto> registrarSignos(@PathVariable Integer id, @Valid @RequestBody SignosRequest d,
                                                     HttpSession sesion, HttpServletRequest peticion) {
        var s = servicio.registrarSignos(sesionActual.requerir(sesion), id, new DatosSignos(d.presionSistolica(),
            d.presionDiastolica(), d.frecuenciaCardiaca(), d.frecuenciaRespiratoria(), d.temperatura(),
            d.saturacionOxigeno()), peticion.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED).body(SignosDto.de(s));
    }

    @GetMapping("/api/historias/{id}/signos-vitales")
    public List<SignosDto> signosDeHistoria(@PathVariable Integer id, HttpSession sesion) {
        return servicio.signosDeHistoria(sesionActual.requerir(sesion), id).stream().map(SignosDto::de).toList();
    }

    @PostMapping("/api/historias/{id}/antecedentes")
    public ResponseEntity<AntecedenteDto> antecedente(@PathVariable Integer id, @Valid @RequestBody AntecedenteRequest d,
                                                      HttpSession sesion, HttpServletRequest peticion) {
        var a = servicio.registrarAntecedente(sesionActual.requerir(sesion), id, d.tipo(), d.descripcion(), peticion.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED).body(new AntecedenteDto(a.getId(), a.getTipo(), a.getDescripcion(), a.getFechaRegistro()));
    }

    @PostMapping("/api/historias/{id}/alergias")
    public ResponseEntity<AlergiaDto> alergia(@PathVariable Integer id, @Valid @RequestBody AlergiaRequest d,
                                              HttpSession sesion, HttpServletRequest peticion) {
        var a = servicio.registrarAlergia(sesionActual.requerir(sesion), id, d.agente(), d.reaccion(), d.gravedad(), peticion.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED).body(new AlergiaDto(a.getId(), a.getAgente(), a.getReaccion(), a.getGravedad()));
    }
}
