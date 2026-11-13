package pe.edu.ucv.hce.aplicacion;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ucv.hce.dominio.DocumentoDuplicadoException;
import pe.edu.ucv.hce.dominio.Establecimiento;
import pe.edu.ucv.hce.dominio.HistoriaClinica;
import pe.edu.ucv.hce.dominio.Paciente;
import pe.edu.ucv.hce.persistencia.HistoriaClinicaRepository;
import pe.edu.ucv.hce.persistencia.PacienteRepository;

/** CU-03: registrar paciente y abrir su historia clinica (RF-01, RF-04; RN-01, RN-06; DEC-02). */
@Service
public class RegistroPacienteService {

    private final PacienteRepository pacientes;
    private final HistoriaClinicaRepository historias;

    public RegistroPacienteService(PacienteRepository pacientes,
                                   HistoriaClinicaRepository historias) {
        this.pacientes = pacientes;
        this.historias = historias;
    }

    public static final String MENSAJE_OTRO_ESTABLECIMIENTO =
        "El paciente ya cuenta con una historia clínica en otro establecimiento. "
        + "Para acceder a ella se requiere un intercambio autorizado por el paciente (RN-06).";

    @Transactional
    public ResultadoRegistro registrarPacienteYAbrirHistoria(Paciente paciente, Establecimiento establecimiento) {
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
                // Flujo alternativo de CU-03: mismo hospital, se muestra la historia existente.
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
        return new ResultadoRegistro(historia, false);
    }

    @Transactional(readOnly = true)
    public List<HistoriaClinica> listarHistorias(Integer idEstablecimiento) {
        return historias.findAllByEstablecimientoIdOrderByIdDesc(idEstablecimiento);
    }
}
