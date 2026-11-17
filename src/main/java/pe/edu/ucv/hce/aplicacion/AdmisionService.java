package pe.edu.ucv.hce.aplicacion;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ucv.hce.dominio.AccesoDenegadoException;
import pe.edu.ucv.hce.dominio.Admision;
import pe.edu.ucv.hce.dominio.CodigoRol;
import pe.edu.ucv.hce.dominio.EstadoAdmision;
import pe.edu.ucv.hce.dominio.HistoriaClinica;
import pe.edu.ucv.hce.dominio.RecursoNoEncontradoException;
import pe.edu.ucv.hce.dominio.RegistroDuplicadoException;
import pe.edu.ucv.hce.dominio.Usuario;
import pe.edu.ucv.hce.persistencia.AdmisionRepository;
import pe.edu.ucv.hce.persistencia.HistoriaClinicaRepository;
import pe.edu.ucv.hce.persistencia.SignosVitalesRepository;

/**
 * CU-20 Admitir al paciente del dia y cola por rol (RF-21, RF-22; RN-13, RN-06, RN-07).
 * El orden de los estados lo impone el dominio (EstadoAdmision, RN-13); aqui se decide QUIEN puede mover cada paso:
 * admision admite y cancela; enfermeria toma y deja listo (CU-21); el medico inicia y cierra la atencion (CU-05).
 * Todo se filtra por el hospital del usuario en sesion (RN-06).
 */
@Service
public class AdmisionService {

    public static final String ACCION_ADMITIR = "ADMITIR_PACIENTE";
    public static final String ACCION_CAMBIAR_ESTADO = "CAMBIAR_ESTADO_ADMISION";

    /** RF-22: lo que ve cada rol en su cola. */
    public static Set<EstadoAdmision> estadosDeLaCola(Usuario u) {
        if (u.tieneRol(CodigoRol.ENFERMERIA)) {
            return EnumSet.of(EstadoAdmision.ADMITIDO, EstadoAdmision.EN_ENFERMERIA);
        }
        if (u.tieneRol(CodigoRol.MEDICO)) {
            return EnumSet.of(EstadoAdmision.LISTO_PARA_ATENCION, EstadoAdmision.EN_ATENCION);
        }
        return EnumSet.allOf(EstadoAdmision.class); // admision: todos los del dia
    }

    private final AdmisionRepository admisiones;
    private final HistoriaClinicaRepository historias;
    private final AutorizacionService autorizacion;
    private final AuditoriaService auditoria;
    private final SignosVitalesRepository signos;

    public AdmisionService(AdmisionRepository admisiones, HistoriaClinicaRepository historias,
                           AutorizacionService autorizacion, AuditoriaService auditoria,
                           SignosVitalesRepository signos) {
        this.signos = signos;
        this.admisiones = admisiones;
        this.historias = historias;
        this.autorizacion = autorizacion;
        this.auditoria = auditoria;
    }

    /** CU-20: admite al paciente en estado ADMITIDO. Si ya tiene una admision activa hoy, se rechaza. */
    @Transactional
    public Admision admitir(Usuario usuario, Integer idHistoria, String tipoAtencion, String ip) {
        autorizacion.exigir(usuario, Permiso.ADMITIR_PACIENTE);
        HistoriaClinica historia = historias.findById(idHistoria)
            .filter(h -> h.getEstablecimiento().getId().equals(hospitalDe(usuario)))
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe la historia clínica pedida en tu hospital."));
        boolean activaHoy = admisiones.findAllByHistoriaClinicaIdAndFechaHoraAdmisionGreaterThanEqual(
            historia.getId(), inicioDelDia()).stream().anyMatch(a -> a.getEstado().esActiva());
        if (activaHoy) {
            throw new RegistroDuplicadoException("El paciente ya fue admitido hoy y sigue en atención.");
        }
        Admision a = admisiones.save(new Admision(historia, tipoAtencion, usuario, LocalDateTime.now()));
        auditoria.registrar(usuario, ACCION_ADMITIR, "admision", a.getId(), historia, ip);
        return a;
    }

    /** RF-22: la cola del dia del rol, la emergencia primero y luego por orden de llegada. */
    @Transactional(readOnly = true)
    public List<Admision> cola(Usuario usuario) {
        autorizacion.exigir(usuario, Permiso.VER_COLA);
        return admisiones.findAllByEstablecimientoIdAndFechaHoraAdmisionGreaterThanEqualAndEstadoIn(
                hospitalDe(usuario), inicioDelDia(), estadosDeLaCola(usuario)).stream()
            .sorted(Comparator.comparing((Admision a) -> !a.esEmergencia())
                .thenComparing(Admision::getFechaHoraAdmision))
            .toList();
    }

    /** CU-20: admision cancela (solo desde ADMITIDO, RN-13). */
    @Transactional
    public Admision cancelar(Usuario usuario, Integer idAdmision, String ip) {
        autorizacion.exigir(usuario, Permiso.ADMITIR_PACIENTE);
        return mover(usuario, idAdmision, EstadoAdmision.CANCELADO, ip);
    }

    /** CU-21 paso 2: enfermeria toma al paciente (ADMITIDO -> EN_ENFERMERIA). */
    @Transactional
    public Admision tomarEnEnfermeria(Usuario usuario, Integer idAdmision, String ip) {
        autorizacion.exigir(usuario, Permiso.REGISTRAR_SIGNOS_VITALES);
        return mover(usuario, idAdmision, EstadoAdmision.EN_ENFERMERIA, ip);
    }

    /**
     * CU-21 paso 5: enfermeria confirma (EN_ENFERMERIA -> LISTO_PARA_ATENCION).
     * RN-17: exige que la admision tenga signos vitales registrados.
     */
    @Transactional
    public Admision marcarListo(Usuario usuario, Integer idAdmision, String ip) {
        autorizacion.exigir(usuario, Permiso.REGISTRAR_SIGNOS_VITALES);
        Admision a = delHospital(usuario, idAdmision);
        if (a.getEstado() == EstadoAdmision.EN_ENFERMERIA && !signos.existsByAdmisionId(a.getId())) {
            throw new IllegalArgumentException("Registra los signos vitales antes de dejar al paciente listo para atención.");
        }
        return mover(usuario, idAdmision, EstadoAdmision.LISTO_PARA_ATENCION, ip);
    }

    /** CU-05 ampliado: el medico comienza a atender (LISTO_PARA_ATENCION -> EN_ATENCION). */
    @Transactional
    public Admision iniciarAtencion(Usuario usuario, Integer idAdmision, String ip) {
        autorizacion.exigir(usuario, Permiso.REGISTRAR_ATENCION);
        return mover(usuario, idAdmision, EstadoAdmision.EN_ATENCION, ip);
    }

    /** CU-05 ampliado: al cerrar la atencion (EN_ATENCION -> ATENCION_CERRADA). */
    @Transactional
    public Admision cerrarAtencion(Usuario usuario, Integer idAdmision, String ip) {
        autorizacion.exigir(usuario, Permiso.REGISTRAR_ATENCION);
        return mover(usuario, idAdmision, EstadoAdmision.ATENCION_CERRADA, ip);
    }

    /** Admision del hospital del usuario (RN-06: la de otro hospital se trata como inexistente). */
    @Transactional(readOnly = true)
    public Admision delHospital(Usuario usuario, Integer idAdmision) {
        autorizacion.exigir(usuario, Permiso.VER_COLA);
        return admisiones.findByIdAndEstablecimientoId(idAdmision, hospitalDe(usuario))
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe la admisión pedida en tu hospital."));
    }

    private Admision mover(Usuario usuario, Integer idAdmision, EstadoAdmision destino, String ip) {
        Admision a = delHospital(usuario, idAdmision);
        a.pasarA(destino, LocalDateTime.now()); // RN-13 en el dominio
        auditoria.registrar(usuario, ACCION_CAMBIAR_ESTADO + ":" + destino.name(), "admision", a.getId(),
            a.getHistoriaClinica(), ip);
        return admisiones.save(a);
    }

    private Integer hospitalDe(Usuario usuario) {
        if (usuario.getEstablecimiento() == null) {
            throw new AccesoDenegadoException("Tu usuario no pertenece a un hospital.");
        }
        return usuario.getEstablecimiento().getId();
    }

    private static LocalDateTime inicioDelDia() {
        return LocalDate.now().atStartOfDay();
    }
}
