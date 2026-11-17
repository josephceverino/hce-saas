package pe.edu.ucv.hce.presentacion;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import pe.edu.ucv.hce.aplicacion.ResumenHistorial;
import pe.edu.ucv.hce.dominio.Alergia;
import pe.edu.ucv.hce.dominio.Antecedente;
import pe.edu.ucv.hce.dominio.Diagnostico;

/** Respuesta de GET /api/mi-historial/resumen (CU-10, RF-16). Contrato: docs/contratos/portal_paciente.md. */
public record ResumenHistorialResponse(
        String nroHc, String paciente, String tipoDocumento, String nroDocumento, LocalDate fechaNacimiento,
        String sexo, String establecimiento, List<AlergiaDto> alergias, List<AntecedenteDto> antecedentes,
        int totalAtenciones, LocalDateTime ultimaAtencion, List<DiagnosticoDto> diagnosticos,
        List<ExamenDto> examenes, LocalDateTime generadoEn) {

    public record AlergiaDto(String agente, String reaccion, String gravedad) { }

    public record AntecedenteDto(String tipo, String descripcion, LocalDate fechaRegistro) { }

    public record DiagnosticoDto(String codigoCie10, String descripcion) { }

    /** R4.2: examen con resultado de una atencion cerrada. */
    public record ExamenDto(String tipoExamen, LocalDate fechaResultado, String resultado) { }

    public static ResumenHistorialResponse de(ResumenHistorial r) {
        var h = r.historia();
        var p = h.getPaciente();
        return new ResumenHistorialResponse(
            h.getNroHc(), p.getApellidos() + ", " + p.getNombres(), p.getTipoDocumento(), p.getNroDocumento(),
            p.getFechaNacimiento(), p.getSexo(), h.getEstablecimiento().getNombre(),
            r.alergias().stream().map((Alergia a) -> new AlergiaDto(a.getAgente(), a.getReaccion(), a.getGravedad())).toList(),
            r.antecedentes().stream()
                .map((Antecedente a) -> new AntecedenteDto(a.getTipo(), a.getDescripcion(), a.getFechaRegistro())).toList(),
            r.totalAtenciones(), r.ultimaAtencion(),
            r.diagnosticos().stream()
                .map((Diagnostico d) -> new DiagnosticoDto(d.getCodigoCie10(), d.getDescripcion())).toList(),
            r.examenes().stream().map(x -> new ExamenDto(x.getTipoExamen(), x.getResultado().getFecha(),
                x.getResultado().getResultado())).toList(),
            r.generadoEn());
    }
}
