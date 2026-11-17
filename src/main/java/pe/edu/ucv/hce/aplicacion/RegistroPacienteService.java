package pe.edu.ucv.hce.aplicacion;

import pe.edu.ucv.hce.dominio.CodigoRol;
import pe.edu.ucv.hce.dominio.RegistroDuplicadoException;
import pe.edu.ucv.hce.persistencia.RolRepository;
import pe.edu.ucv.hce.persistencia.UsuarioRepository;

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
        + "Para acceder a ella se requiere un intercambio autorizado por el paciente.";

    public static final String ACCION_REGISTRAR = "REGISTRAR_PACIENTE_Y_ABRIR_HISTORIA";
    public static final String ACCION_VER_EXISTENTE = "VER_HISTORIA_EXISTENTE";
    public static final String ACCION_CREAR_CUENTA_PORTAL = "CREAR_CUENTA_PORTAL";

    private final PacienteRepository pacientes;
    private final HistoriaClinicaRepository historias;
    private final AutorizacionService autorizacion;
    private final AuditoriaService auditoria;
    private final UsuarioRepository usuarios;
    private final RolRepository roles;

    public RegistroPacienteService(PacienteRepository pacientes, HistoriaClinicaRepository historias,
                                   AutorizacionService autorizacion, AuditoriaService auditoria,
                                   UsuarioRepository usuarios, RolRepository roles) {
        this.usuarios = usuarios;
        this.roles = roles;
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
        // CU-19: la cuenta del portal se crea junto con el registro.
        String clave = crearCuentaPortal(guardado, usuario, ip);
        return new ResultadoRegistro(historia, false, clave == null ? null : guardado.getUsuario().getUsuario(), clave);
    }

    /**
     * CU-19 (RF-27; RN-15, RN-14): cuenta del portal con usuario = documento, sin hospital (el paciente es global,
     * DEC-02) y clave temporal que debe cambiar. Una sola cuenta por paciente: si ya tiene, no se crea otra.
     * Devuelve la clave temporal en texto (solo para mostrarla una vez) o null si no se creo cuenta.
     */
    private String crearCuentaPortal(Paciente paciente, Usuario admision, String ip) {
        if (paciente.getUsuario() != null) {
            return null;
        }
        String nombreUsuario = paciente.getNroDocumento().trim();
        if (usuarios.findByUsuario(nombreUsuario).isPresent()) {
            throw new RegistroDuplicadoException("Ya existe una cuenta con el usuario " + nombreUsuario
                + ". Avisa al administrador del hospital.");
        }
        String clave = ClaveUtil.generarTemporal();
        Usuario cuenta = new Usuario(null, roles.findByNombre(CodigoRol.PACIENTE.name()).orElseThrow(),
            nombreUsuario, ClaveUtil.bcrypt(clave), "ACTIVO");
        cuenta.setDatosPersona(paciente.getApellidos() + ", " + paciente.getNombres(), paciente.getNroDocumento(),
            paciente.getCorreo());
        cuenta.setDebeCambiarClave(true);
        cuenta = usuarios.save(cuenta);
        paciente.vincularUsuario(cuenta);
        pacientes.save(paciente);
        // Sin historia: es un acto sobre la cuenta, no un acceso a la historia clinica (la auditoria del hospital
        // igual lo muestra porque lo hizo un usuario de admision del hospital).
        auditoria.registrar(admision, ACCION_CREAR_CUENTA_PORTAL, "usuario", cuenta.getId(), null, ip);
        return clave;
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
