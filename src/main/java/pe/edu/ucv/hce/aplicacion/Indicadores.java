package pe.edu.ucv.hce.aplicacion;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Formas de respuesta de los indicadores (RF-19, RF-20; DEC-10, DEC-11). Son datos planos, sin entidades.
 * Contrato: docs/contratos/auditoria_indicadores.md.
 */
public final class Indicadores {

    private Indicadores() { }

    /** Una tarjeta del Inicio: valor real de la base de datos, filtrado por hospital. */
    public record Tarjeta(String clave, String titulo, long valor, String nota) { }

    public record Inicio(String establecimiento, List<Tarjeta> tarjetas) { }

    /** Un punto de un grafico de barras: etiqueta de fecha (dia o inicio de semana) y total. */
    public record PuntoFecha(LocalDate fecha, long total) { }

    public record DiagnosticoFrecuente(String codigoCie10, String descripcion, long total) { }

    public record AccesoReciente(LocalDateTime fechaHora, String usuario, String accion, String nroHc) { }

    /** Tablero del Administrador del hospital (DEC-10). */
    public record Tablero(String establecimiento, List<PuntoFecha> atencionesPorDia,
                          List<PuntoFecha> pacientesNuevosPorSemana, List<DiagnosticoFrecuente> diagnosticosFrecuentes,
                          long atencionesAbiertas, long atencionesCerradas, List<AccesoReciente> ultimosAccesos) { }

    /** Panel de plataforma (DEC-11): SOLO totales por hospital; nunca datos clinicos ni de pacientes. */
    public record ResumenHospital(Integer id, String nombre, String codigoIpress, String region, String estado,
                                  String plan, long usuarios, long pacientes, long atenciones) { }
}
