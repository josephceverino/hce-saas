package pe.edu.ucv.hce.aplicacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ucv.hce.dominio.AccesoDenegadoException;
import pe.edu.ucv.hce.dominio.Admision;
import pe.edu.ucv.hce.dominio.Alergia;
import pe.edu.ucv.hce.dominio.Antecedente;
import pe.edu.ucv.hce.dominio.EstadoAdmision;
import pe.edu.ucv.hce.dominio.HistoriaClinica;
import pe.edu.ucv.hce.dominio.RecursoNoEncontradoException;
import pe.edu.ucv.hce.dominio.SignosVitales;
import pe.edu.ucv.hce.dominio.TransicionInvalidaException;
import pe.edu.ucv.hce.dominio.Usuario;
import pe.edu.ucv.hce.persistencia.AlergiaRepository;
import pe.edu.ucv.hce.persistencia.AntecedenteRepository;
import pe.edu.ucv.hce.persistencia.HistoriaClinicaRepository;
import pe.edu.ucv.hce.persistencia.SignosVitalesRepository;

/**
 * CU-21 Atender la cola y registrar signos vitales (RF-23, RF-07; RN-17, RN-06, RN-07) y antecedentes por tipo.
 * Signos vitales: solo enfermeria, sobre una admision de su hospital que este EN_ENFERMERIA (RN-17).
 * Antecedentes y alergias: medico o enfermeria (RF-07), sobre una historia de su hospital (RN-06).
 */
@Service
public class EnfermeriaService {

    public static final String ACCION_SIGNOS = "REGISTRAR_SIGNOS_VITALES";
    public static final String ACCION_ANTECEDENTE = "REGISTRAR_ANTECEDENTE";
    public static final String ACCION_ALERGIA = "REGISTRAR_ALERGIA";
    /** RF-07 ampliado: tipos de antecedente. */
    public static final Set<String> TIPOS_ANTECEDENTE = Set.of("PERSONAL", "QUIRURGICO", "VACUNA", "HABITO", "FAMILIAR");
    public static final Set<String> GRAVEDADES = Set.of("LEVE", "MODERADA", "ALTA");

    public record DatosSignos(Integer presionSistolica, Integer presionDiastolica, Integer frecuenciaCardiaca,
                              Integer frecuenciaRespiratoria, BigDecimal temperatura, Integer saturacionOxigeno) { }

    private final SignosVitalesRepository signos;
    private final AntecedenteRepository antecedentes;
    private final AlergiaRepository alergias;
    private final HistoriaClinicaRepository historias;
    private final AdmisionService admisiones;
    private final AutorizacionService autorizacion;
    private final AuditoriaService auditoria;

    public EnfermeriaService(SignosVitalesRepository signos, AntecedenteRepository antecedentes, AlergiaRepository alergias,
                             HistoriaClinicaRepository historias, AdmisionService admisiones,
                             AutorizacionService autorizacion, AuditoriaService auditoria) {
        this.signos = signos;
        this.antecedentes = antecedentes;
        this.alergias = alergias;
        this.historias = historias;
        this.admisiones = admisiones;
        this.autorizacion = autorizacion;
        this.auditoria = auditoria;
    }

    /** CU-21 paso 3 (RN-17): registra los signos vitales de la admision del dia. */
    @Transactional
    public SignosVitales registrarSignos(Usuario usuario, Integer idAdmision, DatosSignos d, String ip) {
        autorizacion.exigir(usuario, Permiso.REGISTRAR_SIGNOS_VITALES);
        Admision a = admisiones.delHospital(usuario, idAdmision);
        if (a.getEstado() != EstadoAdmision.EN_ENFERMERIA) {
            throw new TransicionInvalidaException("Los signos vitales se registran cuando el paciente está en enfermería. "
                + "Ahora está " + a.getEstado().descripcion().toLowerCase() + ".");
        }
        SignosVitales s = signos.save(new SignosVitales(a, d.presionSistolica(), d.presionDiastolica(),
            d.frecuenciaCardiaca(), d.frecuenciaRespiratoria(), d.temperatura(), d.saturacionOxigeno(), usuario,
            LocalDateTime.now()));
        auditoria.registrar(usuario, ACCION_SIGNOS, "signos_vitales", s.getId(), a.getHistoriaClinica(), ip);
        return s;
    }

    /** P-04: signos vitales de una historia de su hospital (medico o enfermeria). */
    @Transactional(readOnly = true)
    public List<SignosVitales> signosDeHistoria(Usuario usuario, Integer idHistoria) {
        autorizacion.exigir(usuario, Permiso.CONSULTAR_HISTORIA);
        return signos.findAllByAdmisionHistoriaClinicaIdOrderByFechaHoraDesc(historiaDelHospital(usuario, idHistoria).getId());
    }

    /** CU-21 paso 4, RF-07: antecedente por tipo (personal, quirurgico, vacuna, habito, familiar). */
    @Transactional
    public Antecedente registrarAntecedente(Usuario usuario, Integer idHistoria, String tipo, String descripcion, String ip) {
        autorizacion.exigir(usuario, Permiso.REGISTRAR_ANTECEDENTES_ALERGIAS);
        HistoriaClinica h = historiaDelHospital(usuario, idHistoria);
        String t = tipo == null ? "" : tipo.trim().toUpperCase();
        if (!TIPOS_ANTECEDENTE.contains(t)) {
            throw new IllegalArgumentException("Elige un tipo de antecedente válido.");
        }
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("Describe el antecedente.");
        }
        Antecedente a = antecedentes.save(new Antecedente(h, t, descripcion.trim(), LocalDate.now()));
        auditoria.registrar(usuario, ACCION_ANTECEDENTE, "antecedente", a.getId(), h, ip);
        return a;
    }

    /** CU-21 paso 4, RF-07: alergia con su reaccion y gravedad. */
    @Transactional
    public Alergia registrarAlergia(Usuario usuario, Integer idHistoria, String agente, String reaccion,
                                   String gravedad, String ip) {
        autorizacion.exigir(usuario, Permiso.REGISTRAR_ANTECEDENTES_ALERGIAS);
        HistoriaClinica h = historiaDelHospital(usuario, idHistoria);
        String g = gravedad == null ? "" : gravedad.trim().toUpperCase();
        if (!GRAVEDADES.contains(g)) {
            throw new IllegalArgumentException("Elige la gravedad de la alergia.");
        }
        if (agente == null || agente.isBlank() || reaccion == null || reaccion.isBlank()) {
            throw new IllegalArgumentException("Indica a qué es alérgico y qué reacción tiene.");
        }
        Alergia a = alergias.save(new Alergia(h, agente.trim(), reaccion.trim(), g));
        auditoria.registrar(usuario, ACCION_ALERGIA, "alergia", a.getId(), h, ip);
        return a;
    }

    /** RN-06: una historia de otro hospital no se toca (403, como en CU-06). */
    private HistoriaClinica historiaDelHospital(Usuario usuario, Integer idHistoria) {
        if (usuario.getEstablecimiento() == null) {
            throw new AccesoDenegadoException("Tu usuario no pertenece a un hospital.");
        }
        HistoriaClinica h = historias.findById(idHistoria)
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe la historia clínica pedida."));
        if (!h.getEstablecimiento().getId().equals(usuario.getEstablecimiento().getId())) {
            throw new AccesoDenegadoException(HistorialService.MENSAJE_OTRO_HOSPITAL);
        }
        return h;
    }
}
