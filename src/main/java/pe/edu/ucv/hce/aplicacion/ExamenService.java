package pe.edu.ucv.hce.aplicacion;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ucv.hce.dominio.AccesoDenegadoException;
import pe.edu.ucv.hce.dominio.Atencion;
import pe.edu.ucv.hce.dominio.ProfesionalSalud;
import pe.edu.ucv.hce.dominio.RecursoNoEncontradoException;
import pe.edu.ucv.hce.dominio.ResultadoExamen;
import pe.edu.ucv.hce.dominio.SolicitudExamen;
import pe.edu.ucv.hce.dominio.Usuario;
import pe.edu.ucv.hce.persistencia.ProfesionalSaludRepository;
import pe.edu.ucv.hce.persistencia.ResultadoExamenRepository;
import pe.edu.ucv.hce.persistencia.SolicitudExamenRepository;

/**
 * CU-22 Solicitar examenes auxiliares y CU-23 Registrar resultado (RF-25, RF-09; RN-02, RN-03, RN-06, RN-07, RN-18).
 * El medico pide examenes solo sobre una atencion propia y ABIERTA (RN-03); apoyo al diagnostico registra el resultado
 * solo sobre solicitudes de su hospital en estado SOLICITADO (RN-18). El hospital sale de la sesion, nunca del cliente.
 */
@Service
public class ExamenService {

    public static final String ACCION_SOLICITAR = "SOLICITAR_EXAMEN";
    public static final String ACCION_RESULTADO = "REGISTRAR_RESULTADO_EXAMEN";

    private final SolicitudExamenRepository solicitudes;
    private final ResultadoExamenRepository resultados;
    private final ProfesionalSaludRepository profesionales;
    private final AtencionService atenciones;
    private final AutorizacionService autorizacion;
    private final AuditoriaService auditoria;

    public ExamenService(SolicitudExamenRepository solicitudes, ResultadoExamenRepository resultados,
                         ProfesionalSaludRepository profesionales, AtencionService atenciones,
                         AutorizacionService autorizacion, AuditoriaService auditoria) {
        this.solicitudes = solicitudes;
        this.resultados = resultados;
        this.profesionales = profesionales;
        this.atenciones = atenciones;
        this.autorizacion = autorizacion;
        this.auditoria = auditoria;
    }

    /** CU-22: el medico pide un examen en su atencion abierta. */
    @Transactional
    public SolicitudExamen solicitar(Usuario medico, Integer idAtencion, String tipoExamen, String indicaciones, String ip) {
        autorizacion.exigir(medico, Permiso.SOLICITAR_EXAMEN);
        if (tipoExamen == null || tipoExamen.isBlank()) {
            throw new IllegalArgumentException("Ingresa el tipo de examen.");
        }
        Atencion atencion = atenciones.atencionPropiaAbierta(medico, idAtencion); // RN-06, RN-11, RN-03
        ProfesionalSalud solicitante = profesionalDe(medico);
        SolicitudExamen s = solicitudes.save(new SolicitudExamen(atencion, solicitante, tipoExamen.trim(),
            indicaciones == null || indicaciones.isBlank() ? null : indicaciones.trim(), LocalDateTime.now()));
        auditoria.registrar(medico, ACCION_SOLICITAR, "solicitud_examen", s.getId(), atencion.getHistoriaClinica(), ip);
        return s;
    }

    /** CU-23 paso 1: las solicitudes del hospital de apoyo al diagnostico en un estado (pendientes o ya resueltas). */
    @Transactional(readOnly = true)
    public List<SolicitudExamen> delHospital(Usuario apoyo, String estado) {
        autorizacion.exigir(apoyo, Permiso.REGISTRAR_RESULTADO_EXAMEN);
        String filtro = SolicitudExamen.RESULTADO_REGISTRADO.equals(estado)
            ? SolicitudExamen.RESULTADO_REGISTRADO : SolicitudExamen.SOLICITADO;
        return solicitudes.findAllByEstablecimientoIdAndEstadoOrderByFechaHoraAsc(hospitalDe(apoyo), filtro);
    }

    /** CU-23: apoyo al diagnostico registra el resultado de una solicitud pendiente de su hospital. */
    @Transactional
    public SolicitudExamen registrarResultado(Usuario apoyo, Integer idSolicitud, String texto, String ip) {
        autorizacion.exigir(apoyo, Permiso.REGISTRAR_RESULTADO_EXAMEN);
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Ingresa el resultado del examen.");
        }
        SolicitudExamen s = solicitudes.findByIdAndEstablecimientoId(idSolicitud, hospitalDe(apoyo))
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe la solicitud de examen pedida en tu hospital."));
        ResultadoExamen r = new ResultadoExamen(s, profesionalDe(apoyo), LocalDate.now(), texto.trim());
        s.registrarResultado(r); // RN-18: solo desde SOLICITADO
        resultados.save(r);
        solicitudes.save(s);
        auditoria.registrar(apoyo, ACCION_RESULTADO, "solicitud_examen", s.getId(),
            s.getAtencion().getHistoriaClinica(), ip);
        return s;
    }

    private ProfesionalSalud profesionalDe(Usuario u) {
        return profesionales.findByUsuarioId(u.getId())
            .orElseThrow(() -> new AccesoDenegadoException("Tu usuario no tiene un perfil de profesional de salud."));
    }

    private static Integer hospitalDe(Usuario u) {
        if (u.getEstablecimiento() == null) {
            throw new AccesoDenegadoException("Tu usuario no pertenece a un hospital.");
        }
        return u.getEstablecimiento().getId();
    }
}
