package pe.edu.ucv.hce.aplicacion;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ucv.hce.dominio.AccesoDenegadoException;
import pe.edu.ucv.hce.dominio.ConsentimientoAcceso;
import pe.edu.ucv.hce.dominio.HistoriaClinica;
import pe.edu.ucv.hce.dominio.Paciente;
import pe.edu.ucv.hce.dominio.RecursoNoEncontradoException;
import pe.edu.ucv.hce.dominio.Usuario;
import pe.edu.ucv.hce.persistencia.ConsentimientoAccesoRepository;
import pe.edu.ucv.hce.persistencia.HistoriaClinicaRepository;
import pe.edu.ucv.hce.persistencia.PacienteRepository;

/**
 * CU-11 Gestionar consentimiento de acceso (RF-17; RN-04, RN-06, RN-07, RN-09). Implementado por Pablo (M-5).
 * Todos los metodos exigen Permiso.GESTIONAR_CONSENTIMIENTO (solo PACIENTE; otro rol: AccesoDenegadoException, 403)
 * y trabajan SOLO sobre el paciente dueño de la cuenta en sesion (RN-04): se busca con PacienteRepository.findByUsuarioId,
 * nunca con un id de paciente que mande el cliente. Cuenta sin paciente vinculado: AccesoDenegadoException.
 * Cada accion se audita con AuditoriaService (RN-07), ligada a la historia del paciente si existe.
 * El intercambio real entre hospitales (RF-18) esta fuera del alcance (DEC-02): aqui solo se registra la decision.
 */
@Service
public class ConsentimientoService {

    public static final String ACCION_OTORGAR = "OTORGAR_CONSENTIMIENTO";
    public static final String ACCION_REVOCAR = "REVOCAR_CONSENTIMIENTO";
    public static final String ACCION_LISTAR = "CONSULTAR_CONSENTIMIENTOS";

    private final ConsentimientoAccesoRepository consentimientos;
    private final PacienteRepository pacientes;
    private final HistoriaClinicaRepository historias;
    private final AutorizacionService autorizacion;
    private final AuditoriaService auditoria;

    public ConsentimientoService(ConsentimientoAccesoRepository consentimientos, PacienteRepository pacientes,
                                 HistoriaClinicaRepository historias, AutorizacionService autorizacion,
                                 AuditoriaService auditoria) {
        this.consentimientos = consentimientos;
        this.pacientes = pacientes;
        this.historias = historias;
        this.autorizacion = autorizacion;
        this.auditoria = auditoria;
    }

    /** Todos mis consentimientos (activos y revocados), el mas reciente primero. */
    @Transactional
    public List<ConsentimientoAcceso> listar(Usuario usuario, String ip) {
        Paciente paciente = pacienteDe(usuario);
        List<ConsentimientoAcceso> mios = consentimientos.findAllByPacienteIdOrderByFechaOtorgadoDesc(paciente.getId());
        // id_entidad_afectada no admite nulo: al listar, la entidad consultada es el propio paciente.
        auditoria.registrar(usuario, ACCION_LISTAR, "paciente", paciente.getId(), historiaDe(paciente), ip);
        return mios;
    }

    /**
     * Otorga un consentimiento ACTIVO con fecha y alcance (RNF-02, RN-09: finalidad declarada en `alcance`).
     * otorgadoA o alcance vacios: IllegalArgumentException (400).
     */
    @Transactional
    public ConsentimientoAcceso otorgar(Usuario usuario, String otorgadoA, String alcance, String ip) {
        Paciente paciente = pacienteDe(usuario);
        exigirTexto(otorgadoA, "Indica a quién das el acceso.");
        exigirTexto(alcance, "Indica el alcance del acceso.");
        ConsentimientoAcceso c = consentimientos.save(
            new ConsentimientoAcceso(paciente, otorgadoA.trim(), alcance.trim(), LocalDateTime.now()));
        auditoria.registrar(usuario, ACCION_OTORGAR, "consentimiento_acceso", c.getId(), historiaDe(paciente), ip);
        return c;
    }

    /**
     * Revoca un consentimiento propio (el acceso del tercero termina de inmediato). Guarda la fecha de revocacion.
     * De otro paciente o inexistente: RecursoNoEncontradoException (404). Ya revocado: IllegalArgumentException (400).
     */
    @Transactional
    public ConsentimientoAcceso revocar(Usuario usuario, Integer idConsentimiento, String ip) {
        Paciente paciente = pacienteDe(usuario);
        ConsentimientoAcceso c = consentimientos.findByIdAndPacienteId(idConsentimiento, paciente.getId())
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe el consentimiento pedido."));
        c.revocar(LocalDateTime.now());
        consentimientos.save(c);
        auditoria.registrar(usuario, ACCION_REVOCAR, "consentimiento_acceso", c.getId(), historiaDe(paciente), ip);
        return c;
    }

    /** RN-04: el paciente dueño de esta cuenta. Otro rol o cuenta sin paciente: se rechaza. */
    private Paciente pacienteDe(Usuario usuario) {
        autorizacion.exigir(usuario, Permiso.GESTIONAR_CONSENTIMIENTO);
        return pacientes.findByUsuarioId(usuario.getId())
            .orElseThrow(() -> new AccesoDenegadoException("Tu cuenta no está vinculada a un paciente."));
    }

    private HistoriaClinica historiaDe(Paciente paciente) {
        return historias.findByPacienteId(paciente.getId()).orElse(null);
    }

    private static void exigirTexto(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
    }
}
