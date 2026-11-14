package pe.edu.ucv.hce.aplicacion;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ucv.hce.dominio.AccesoDenegadoException;
import pe.edu.ucv.hce.dominio.DocumentoDuplicadoException;
import pe.edu.ucv.hce.dominio.Establecimiento;
import pe.edu.ucv.hce.dominio.HistoriaClinica;
import pe.edu.ucv.hce.dominio.Paciente;
import pe.edu.ucv.hce.dominio.Usuario;
import pe.edu.ucv.hce.persistencia.HistoriaClinicaRepository;
import pe.edu.ucv.hce.persistencia.PacienteRepository;

/**
 * CU-03: registrar paciente y abrir su historia clinica (RF-01, RF-04; RN-01, RN-06, RN-07; DEC-02, DEC-03, DEC-06).
 * Solo ADMISION registra (RF-01); el hospital sale siempre del usuario en sesion (multi-tenant).
 */
@Service
public class RegistroPacienteService {

    public static final String MENSAJE_OTRO_ESTABLECIMIENTO =
        "El paciente ya cuenta con una historia clínica en otro establecimiento. "
        + "Para acceder a ella se requiere un intercambio autorizado por el paciente (RN-06).";

    public static final String ACCION_REGISTRAR = "REGISTRAR_PACIENTE_Y_ABRIR_HISTORIA";
    public static final String ACCION_VER_EXISTENTE = "VER_HISTORIA_EXISTENTE";

    private final PacienteRepository pacientes;
    private final HistoriaClinicaRepository historias;
    private final AutorizacionService autorizacion;
    private final AuditoriaService auditoria;

    public RegistroPacienteService(PacienteRepository pacientes, HistoriaClinicaRepository historias,
                                   AutorizacionService autorizacion, AuditoriaService auditoria) {
        this.pacientes = pacientes;
        this.historias = historias;
        this.autorizacion = autorizacion;
        this.auditoria = auditoria;
    }

    @Transactional
    public ResultadoRegistro registrarPacienteYAbrirHistoria(Paciente paciente, Usuario usuario, String ip) {
        autorizacion.exigir(usuario, Permiso.REGISTRAR_PACIENTE);
        Establecimiento establecimiento = hospitalDe(usuario);
        String tipo = paciente.getTipoDocumento();
        String nro = paciente.getNroDocumento();

        if ("DNI".equals(tipo) && !nro.matches("\\d{8}")) {
            throw new IllegalArgumentException("El DNI debe tener 8 digitos.");
        }
        // RN-01: una unica historia por documento. DEC-02: el paciente es global.
        var existente = historias.findByPacienteTipoDocumentoAndPacienteNroDocumento(tipo, nro);
        if (existente.isPresent()) {
            HistoriaClinica h = existente.get();
            if (h.getEstablecimiento().getId().equals(establecimiento.getId())) {
                // Flujo alternativo de CU-03: mismo hospital, se muestra la historia existente (RN-07: es un acceso).
                auditoria.registrar(usuario, ACCION_VER_EXISTENTE, "historia_clinica", h.getId(), h, ip);
                return new ResultadoRegistro(h, true);
            }
            // RN-06: otro hospital. Se rechaza sin revelar ningun dato del paciente.
            throw new DocumentoDuplicadoException(MENSAJE_OTRO_ESTABLECIMIENTO);
        }
        if (pacientes.existsByTipoDocumentoAndNroDocumento(tipo, nro)) {
            throw new DocumentoDuplicadoException(MENSAJE_OTRO_ESTABLECIMIENTO);
        }
        Paciente guardado = pacientes.save(paciente);

        HistoriaClinica historia = historias.save(new HistoriaClinica(
            guardado, establecimiento, "TMP-" + System.nanoTime(), LocalDate.now(), "ABIERTA"));
        historia.setNroHc(String.format("HC-%06d", historia.getId()));
        // CU-03 paso 6: se registra la auditoria.
        auditoria.registrar(usuario, ACCION_REGISTRAR, "historia_clinica", historia.getId(), historia, ip);
        return new ResultadoRegistro(historia, false);
    }

    /** RF-02 / RN-06: lista solo las historias del hospital del usuario. */
    @Transactional(readOnly = true)
    public List<HistoriaClinica> listarHistorias(Usuario usuario) {
        autorizacion.exigir(usuario, Permiso.BUSCAR_PACIENTE);
        return historias.findAllByEstablecimientoIdOrderByIdDesc(hospitalDe(usuario).getId());
    }

    private Establecimiento hospitalDe(Usuario usuario) {
        if (usuario.getEstablecimiento() == null) {
            throw new AccesoDenegadoException("Tu usuario no pertenece a un hospital.");
        }
        return usuario.getEstablecimiento();
    }
}
