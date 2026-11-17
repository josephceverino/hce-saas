package pe.edu.ucv.hce.aplicacion;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ucv.hce.aplicacion.Indicadores.AccesoReciente;
import pe.edu.ucv.hce.aplicacion.Indicadores.DiagnosticoFrecuente;
import pe.edu.ucv.hce.aplicacion.Indicadores.Inicio;
import pe.edu.ucv.hce.aplicacion.Indicadores.PuntoFecha;
import pe.edu.ucv.hce.aplicacion.Indicadores.ResumenHospital;
import pe.edu.ucv.hce.aplicacion.Indicadores.Tablero;
import pe.edu.ucv.hce.aplicacion.Indicadores.Tarjeta;
import pe.edu.ucv.hce.dominio.AccesoDenegadoException;
import pe.edu.ucv.hce.dominio.Atencion;
import pe.edu.ucv.hce.dominio.CodigoRol;
import pe.edu.ucv.hce.dominio.HistoriaClinica;
import pe.edu.ucv.hce.dominio.Usuario;
import pe.edu.ucv.hce.persistencia.AtencionRepository;
import pe.edu.ucv.hce.persistencia.DiagnosticoRepository;
import pe.edu.ucv.hce.persistencia.EstablecimientoRepository;
import pe.edu.ucv.hce.persistencia.HistoriaClinicaRepository;
import pe.edu.ucv.hce.persistencia.ProfesionalSaludRepository;
import pe.edu.ucv.hce.persistencia.SuscripcionRepository;
import pe.edu.ucv.hce.persistencia.UsuarioRepository;

/**
 * Indicadores reales para el Inicio por rol, el tablero del Administrador del hospital y el panel de plataforma
 * (RF-19, RF-20; DEC-10, DEC-11; docs/diseno_ui.md). Toda cifra sale de la base de datos y se filtra por el hospital
 * del usuario en sesion (RN-06). El panel de plataforma es la UNICA vista que cruza hospitales y solo da totales.
 */
@Service
public class IndicadoresService {

    public static final int DIAS_GRAFICO = 14;
    public static final int SEMANAS_GRAFICO = 8;
    public static final int TOP_DIAGNOSTICOS = 5;
    public static final int ULTIMOS_ACCESOS = 5;

    private final HistoriaClinicaRepository historias;
    private final AtencionRepository atenciones;
    private final DiagnosticoRepository diagnosticos;
    private final UsuarioRepository usuarios;
    private final EstablecimientoRepository establecimientos;
    private final ProfesionalSaludRepository profesionales;
    private final AuditoriaService auditoria;
    private final AutorizacionService autorizacion;

    private final SuscripcionRepository suscripciones;
    private final pe.edu.ucv.hce.persistencia.SolicitudExamenRepository examenes;

    public IndicadoresService(HistoriaClinicaRepository historias, AtencionRepository atenciones,
                              DiagnosticoRepository diagnosticos, UsuarioRepository usuarios,
                              EstablecimientoRepository establecimientos, ProfesionalSaludRepository profesionales,
                              AuditoriaService auditoria, AutorizacionService autorizacion,
                              SuscripcionRepository suscripciones,
                              pe.edu.ucv.hce.persistencia.SolicitudExamenRepository examenes) {
        this.examenes = examenes;
        this.suscripciones = suscripciones;
        this.historias = historias;
        this.atenciones = atenciones;
        this.diagnosticos = diagnosticos;
        this.usuarios = usuarios;
        this.establecimientos = establecimientos;
        this.profesionales = profesionales;
        this.auditoria = auditoria;
        this.autorizacion = autorizacion;
    }

    /**
     * Tarjetas del Inicio segun el rol (admision, medico, enfermeria, apoyo al diagnostico, admin del hospital).
     * Plan v3, 0.4: cada cifra sale de la base de datos del hospital del usuario. Paciente y plataforma: 403.
     */
    @Transactional(readOnly = true)
    public Inicio inicio(Usuario usuario) {
        boolean apoyo = usuario.tieneRol(CodigoRol.APOYO_DIAGNOSTICO) && usuario.getEstablecimiento() != null;
        if (!apoyo && !autorizacion.puede(usuario, Permiso.BUSCAR_PACIENTE)
                && !autorizacion.puede(usuario, Permiso.VER_REPORTES)) {
            throw new AccesoDenegadoException(AutorizacionService.MENSAJE_SIN_PERMISO);
        }
        Integer h = hospitalDe(usuario);
        LocalDateTime hoy = LocalDate.now().atStartOfDay();
        long pacientes = historias.countByEstablecimientoId(h);
        long historiasAbiertas = historias.countByEstablecimientoIdAndEstado(h, "ABIERTA");
        List<Tarjeta> tarjetas = new ArrayList<>();
        if (usuario.tieneRol(CodigoRol.ADMISION)) {
            tarjetas.add(new Tarjeta("pacientes", "Pacientes registrados", pacientes, "En tu hospital"));
            tarjetas.add(new Tarjeta("historias-abiertas", "Historias abiertas", historiasAbiertas, "En tu hospital"));
            tarjetas.add(new Tarjeta("registrados-hoy", "Registrados hoy",
                historias.countByEstablecimientoIdAndFechaApertura(h, LocalDate.now()), "Historias abiertas hoy"));
        } else if (usuario.tieneRol(CodigoRol.MEDICO)) {
            Integer idProfesional = profesionales.findByUsuarioId(usuario.getId()).map(p -> p.getId()).orElse(-1);
            tarjetas.add(new Tarjeta("mis-atenciones-hoy", "Mis atenciones de hoy",
                atenciones.countByProfesionalIdAndFechaHoraGreaterThanEqual(idProfesional, hoy), "Registradas por ti hoy"));
            tarjetas.add(new Tarjeta("abiertas-por-cerrar", "Atenciones abiertas por cerrar",
                atenciones.countByProfesionalIdAndEstado(idProfesional, Atencion.ABIERTA), "Pendientes de cerrar"));
            tarjetas.add(new Tarjeta("pacientes", "Pacientes del hospital", pacientes, "En tu hospital"));
        } else if (apoyo) {
            tarjetas.add(new Tarjeta("examenes-pendientes", "Exámenes pendientes",
                examenes.countByEstablecimientoIdAndEstado(h, pe.edu.ucv.hce.dominio.SolicitudExamen.SOLICITADO),
                "Esperan su resultado"));
            tarjetas.add(new Tarjeta("examenes-resueltos", "Resultados registrados",
                examenes.countByEstablecimientoIdAndEstado(h, pe.edu.ucv.hce.dominio.SolicitudExamen.RESULTADO_REGISTRADO),
                "En tu hospital"));
        } else if (usuario.tieneRol(CodigoRol.ENFERMERIA)) {
            tarjetas.add(new Tarjeta("pacientes", "Pacientes del hospital", pacientes, "En tu hospital"));
            tarjetas.add(new Tarjeta("historias-abiertas", "Historias abiertas", historiasAbiertas, "En tu hospital"));
        } else { // ADMIN_HOSPITAL
            tarjetas.add(new Tarjeta("pacientes", "Pacientes registrados", pacientes, "En tu hospital"));
            tarjetas.add(new Tarjeta("atenciones-hoy", "Atenciones de hoy",
                atenciones.countByEstablecimientoIdAndFechaHoraGreaterThanEqual(h, hoy), "En tu hospital"));
            tarjetas.add(new Tarjeta("atenciones-abiertas", "Atenciones abiertas",
                atenciones.countByEstablecimientoIdAndEstado(h, Atencion.ABIERTA), "Sin cerrar"));
            tarjetas.add(new Tarjeta("usuarios", "Usuarios del hospital", usuarios.countByEstablecimientoId(h),
                "Cuentas activas e inactivas"));
        }
        return new Inicio(usuario.getEstablecimiento().getNombre(), tarjetas);
    }

    /** Tablero con graficos del Administrador del hospital (RF-19, DEC-10). */
    @Transactional(readOnly = true)
    public Tablero tablero(Usuario usuario) {
        autorizacion.exigir(usuario, Permiso.VER_REPORTES);
        Integer h = hospitalDe(usuario);
        LocalDate hoy = LocalDate.now();

        // Atenciones por dia: ultimos 14 dias, con ceros donde no hubo (para que el grafico no mienta)
        LocalDate desdeDias = hoy.minusDays(DIAS_GRAFICO - 1L);
        Map<LocalDate, Long> porDia = new TreeMap<>();
        for (int i = 0; i < DIAS_GRAFICO; i++) {
            porDia.put(desdeDias.plusDays(i), 0L);
        }
        for (Atencion a : atenciones.findAllByEstablecimientoIdAndFechaHoraGreaterThanEqual(h, desdeDias.atStartOfDay())) {
            porDia.merge(a.getFechaHora().toLocalDate(), 1L, Long::sum);
        }

        // Pacientes nuevos por semana (semanas que empiezan en lunes): ultimas 8
        LocalDate lunesActual = hoy.with(DayOfWeek.MONDAY);
        LocalDate desdeSemanas = lunesActual.minusWeeks(SEMANAS_GRAFICO - 1L);
        Map<LocalDate, Long> porSemana = new TreeMap<>();
        for (int i = 0; i < SEMANAS_GRAFICO; i++) {
            porSemana.put(desdeSemanas.plusWeeks(i), 0L);
        }
        for (HistoriaClinica hc : historias.findAllByEstablecimientoIdAndFechaAperturaGreaterThanEqual(h, desdeSemanas)) {
            porSemana.merge(hc.getFechaApertura().with(DayOfWeek.MONDAY), 1L, Long::sum);
        }

        List<DiagnosticoFrecuente> top = diagnosticos.masFrecuentes(h, PageRequest.of(0, TOP_DIAGNOSTICOS)).stream()
            .map(f -> new DiagnosticoFrecuente((String) f[0], (String) f[1], ((Number) f[2]).longValue())).toList();

        List<AccesoReciente> accesos = auditoria.consultar(usuario,
                new FiltroAuditoria(null, null, null, null, null), 0, AuditoriaService.TAMANO_MAXIMO)
            .registros().stream()
            .filter(r -> r.getHistoriaClinica() != null) // "accesos": lo que toca una historia
            .limit(ULTIMOS_ACCESOS)
            .map(r -> new AccesoReciente(r.getFechaHora(), r.getUsuario().getUsuario(), r.getAccion(),
                r.getHistoriaClinica().getNroHc()))
            .toList();

        return new Tablero(usuario.getEstablecimiento().getNombre(),
            porDia.entrySet().stream().map(e -> new PuntoFecha(e.getKey(), e.getValue())).toList(),
            porSemana.entrySet().stream().map(e -> new PuntoFecha(e.getKey(), e.getValue())).toList(),
            top, atenciones.countByEstablecimientoIdAndEstado(h, Atencion.ABIERTA),
            atenciones.countByEstablecimientoIdAndEstado(h, Atencion.CERRADA), accesos);
    }

    /**
     * Panel de plataforma (RF-20, DEC-11): una fila por hospital con SOLO totales. No incluye ningun dato clinico ni
     * de pacientes. Es la unica vista que cruza hospitales y solo la ve el Administrador de plataforma.
     */
    @Transactional(readOnly = true)
    public List<ResumenHospital> hospitales(Usuario usuario) {
        autorizacion.exigir(usuario, Permiso.GESTIONAR_HOSPITALES);
        return establecimientos.findAll().stream()
            .map(e -> new ResumenHospital(e.getId(), e.getNombre(), e.getCodigoIpress(), e.getRegion(), e.getEstado(),
                suscripciones.findFirstByEstablecimientoIdOrderByFechaInicioDescIdDesc(e.getId())
                    .map(x -> x.getPlan()).orElse(null),
                usuarios.countByEstablecimientoId(e.getId()), historias.countByEstablecimientoId(e.getId()),
                atenciones.countByEstablecimientoId(e.getId())))
            .toList();
    }

    private Integer hospitalDe(Usuario usuario) {
        if (usuario.getEstablecimiento() == null) {
            throw new AccesoDenegadoException("Tu usuario no pertenece a un hospital.");
        }
        return usuario.getEstablecimiento().getId();
    }
}
