package pe.edu.ucv.hce.presentacion;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import pe.edu.ucv.hce.aplicacion.AtencionDetalle;
import pe.edu.ucv.hce.aplicacion.HistorialClinico;
import pe.edu.ucv.hce.dominio.Alergia;
import pe.edu.ucv.hce.dominio.Antecedente;
import pe.edu.ucv.hce.dominio.Diagnostico;
import pe.edu.ucv.hce.dominio.Enmienda;
import pe.edu.ucv.hce.dominio.HistoriaClinica;
import pe.edu.ucv.hce.dominio.Paciente;
import pe.edu.ucv.hce.dominio.Tratamiento;

/**
 * Respuesta de GET /api/historias/{id} (CU-06) y de las operaciones de atencion (CU-05, CU-08).
 * Contrato para el frontend: ver docs/contratos/atencion_historia.md.
 */
public record HistoriaDetalleResponse(
        Integer idHistoriaClinica, String nroHc, String estado, LocalDate fechaApertura, String establecimiento,
        PacienteDto paciente, List<AlergiaDto> alergias, List<AntecedenteDto> antecedentes,
        List<AtencionDto> atenciones) {

    public record PacienteDto(String tipoDocumento, String nroDocumento, String nombres, String apellidos,
                              LocalDate fechaNacimiento, String sexo) { }

    public record AlergiaDto(String agente, String reaccion, String gravedad) { }

    public record AntecedenteDto(String tipo, String descripcion, LocalDate fechaRegistro) { }

    public record DiagnosticoDto(Integer id, String codigoCie10, String descripcion, String tipo) { }

    public record TratamientoDto(Integer id, String indicacion, String medicamento, String dosis, String duracion) { }

    public record EnmiendaDto(Integer id, String motivo, String contenidoCorregido, LocalDateTime fecha,
                              String autor) { }

    /** estado: ABIERTA o CERRADA. cerrada = true muestra el candado y oculta el boton de editar. */
    public record AtencionDto(Integer id, LocalDateTime fechaHora, String motivo, String anamnesis,
                              String examenFisico, String estado, boolean cerrada, String profesional,
                              List<DiagnosticoDto> diagnosticos, List<TratamientoDto> tratamientos,
                              List<EnmiendaDto> enmiendas) { }

    public static HistoriaDetalleResponse de(HistorialClinico h) {
        HistoriaClinica hc = h.historia();
        Paciente p = hc.getPaciente();
        return new HistoriaDetalleResponse(
            hc.getId(), hc.getNroHc(), hc.getEstado(), hc.getFechaApertura(), hc.getEstablecimiento().getNombre(),
            new PacienteDto(p.getTipoDocumento(), p.getNroDocumento(), p.getNombres(), p.getApellidos(),
                p.getFechaNacimiento(), p.getSexo()),
            h.alergias().stream().map(HistoriaDetalleResponse::alergia).toList(),
            h.antecedentes().stream().map(HistoriaDetalleResponse::antecedente).toList(),
            h.atenciones().stream().map(HistoriaDetalleResponse::atencion).toList());
    }

    public static AtencionDto atencion(AtencionDetalle d) {
        var a = d.atencion();
        return new AtencionDto(a.getId(), a.getFechaHora(), a.getMotivo(), a.getAnamnesis(), a.getExamenFisico(),
            a.getEstado(), a.estaCerrada(),
            a.getProfesional().getApellidos() + ", " + a.getProfesional().getNombres(),
            d.diagnosticos().stream().map(HistoriaDetalleResponse::diagnostico).toList(),
            d.tratamientos().stream().map(HistoriaDetalleResponse::tratamiento).toList(),
            d.enmiendas().stream().map(HistoriaDetalleResponse::enmienda).toList());
    }

    public static DiagnosticoDto diagnostico(Diagnostico x) {
        return new DiagnosticoDto(x.getId(), x.getCodigoCie10(), x.getDescripcion(), x.getTipo());
    }

    public static TratamientoDto tratamiento(Tratamiento x) {
        return new TratamientoDto(x.getId(), x.getIndicacion(), x.getMedicamento(), x.getDosis(), x.getDuracion());
    }

    public static EnmiendaDto enmienda(Enmienda x) {
        return new EnmiendaDto(x.getId(), x.getMotivo(), x.getContenidoCorregido(), x.getFecha(),
            x.getProfesional().getApellidos() + ", " + x.getProfesional().getNombres());
    }

    private static AlergiaDto alergia(Alergia x) {
        return new AlergiaDto(x.getAgente(), x.getReaccion(), x.getGravedad());
    }

    private static AntecedenteDto antecedente(Antecedente x) {
        return new AntecedenteDto(x.getTipo(), x.getDescripcion(), x.getFechaRegistro());
    }
}
