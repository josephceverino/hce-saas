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

/** CU-03: registrar paciente y abrir su historia clinica (RF-01 y RF-04). */
@Service
public class RegistroPacienteService {

    private final PacienteRepository pacientes;
    private final HistoriaClinicaRepository historias;

    public RegistroPacienteService(PacienteRepository pacientes,
                                   HistoriaClinicaRepository historias) {
        this.pacientes = pacientes;
        this.historias = historias;
    }

    @Transactional
    public HistoriaClinica registrarPacienteYAbrirHistoria(Paciente paciente, Establecimiento establecimiento) {
        String tipo = paciente.getTipoDocumento();
        String nro = paciente.getNroDocumento();

        if ("DNI".equals(tipo) && !nro.matches("\\d{8}")) {
            throw new IllegalArgumentException("El DNI debe tener 8 digitos.");
        }
        // RN-01: un paciente no puede tener dos historias clinicas.
        if (pacientes.existsByTipoDocumentoAndNroDocumento(tipo, nro)) {
            throw new DocumentoDuplicadoException(
                "Ya existe un paciente con " + tipo + " " + nro + ". Cada paciente tiene una unica historia clinica.");
        }
        Paciente guardado = pacientes.save(paciente);

        HistoriaClinica historia = historias.save(new HistoriaClinica(
            guardado, establecimiento, "TMP-" + System.nanoTime(), LocalDate.now(), "ABIERTA"));
        historia.setNroHc(String.format("HC-%06d", historia.getId()));
        return historia;
    }

    @Transactional(readOnly = true)
    public List<HistoriaClinica> listarHistorias(Integer idEstablecimiento) {
        return historias.findAllByEstablecimientoIdOrderByIdDesc(idEstablecimiento);
    }
}
