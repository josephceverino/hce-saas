package pe.edu.ucv.hce.aplicacion;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ucv.hce.dominio.AccesoDenegadoException;
import pe.edu.ucv.hce.dominio.Diagnostico;
import pe.edu.ucv.hce.dominio.HistoriaClinica;
import pe.edu.ucv.hce.dominio.Paciente;
import pe.edu.ucv.hce.dominio.RecursoNoEncontradoException;
import pe.edu.ucv.hce.dominio.Usuario;
import pe.edu.ucv.hce.persistencia.HistoriaClinicaRepository;
import pe.edu.ucv.hce.persistencia.PacienteRepository;

/**
 * CU-10 Consultar mi historial clinico (RF-14, RF-15, RF-16; RN-04, RN-07). Portal del paciente.
 * RN-04: el paciente solo llega a SU historia (se busca por su cuenta, nunca por un id que mande el cliente)
 * y solo en lectura (este servicio no tiene ninguna operacion de escritura).
 * RN-05 (representante legal) no esta implementada en el prototipo (DEC-05).
 * DEC-13: el paciente ve solo atenciones CERRADAS; las abiertas son borradores del medico.
 */
@Service
public class PortalPacienteService {

    public static final String ACCION_MI_HISTORIAL = "CONSULTAR_MI_HISTORIAL";
    public static final String ACCION_MI_RESUMEN = "VER_MI_RESUMEN";

    private final PacienteRepository pacientes;
    private final HistoriaClinicaRepository historias;
    private final HistorialService historial;
    private final AutorizacionService autorizacion;
    private final AuditoriaService auditoria;

    public PortalPacienteService(PacienteRepository pacientes, HistoriaClinicaRepository historias,
                                 HistorialService historial, AutorizacionService autorizacion,
                                 AuditoriaService auditoria) {
        this.pacientes = pacientes;
        this.historias = historias;
        this.historial = historial;
        this.autorizacion = autorizacion;
        this.auditoria = auditoria;
    }

    /** Mi historial en solo lectura: antecedentes, alergias y atenciones cerradas con sus diagnosticos y tratamientos. */
    @Transactional
    public HistorialClinico miHistorial(Usuario usuario, String ip) {
        HistoriaClinica historia = historiaPropia(usuario);
        HistorialClinico completo = historial.cargar(historia);
        List<AtencionDetalle> cerradas = completo.atenciones().stream()
            .filter(a -> a.atencion().estaCerrada()).toList();
        auditoria.registrar(usuario, ACCION_MI_HISTORIAL, "historia_clinica", historia.getId(), historia, ip);
        return new HistorialClinico(historia, completo.antecedentes(), completo.alergias(), cerradas);
    }

    /** Resumen para ver en pantalla o imprimir/descargar (RF-16). */
    @Transactional
    public ResumenHistorial miResumen(Usuario usuario, String ip) {
        HistoriaClinica historia = historiaPropia(usuario);
        HistorialClinico completo = historial.cargar(historia);
        List<AtencionDetalle> cerradas = completo.atenciones().stream()
            .filter(a -> a.atencion().estaCerrada()).toList();
        Map<String, Diagnostico> distintos = new LinkedHashMap<>();
        for (AtencionDetalle a : cerradas) { // de la mas reciente a la mas antigua
            for (Diagnostico d : a.diagnosticos()) {
                distintos.putIfAbsent(d.getCodigoCie10(), d);
            }
        }
        LocalDateTime ultima = cerradas.isEmpty() ? null : cerradas.get(0).atencion().getFechaHora();
        auditoria.registrar(usuario, ACCION_MI_RESUMEN, "historia_clinica", historia.getId(), historia, ip);
        return new ResumenHistorial(historia, completo.alergias(), completo.antecedentes(), cerradas.size(),
            ultima, new ArrayList<>(distintos.values()), LocalDateTime.now());
    }

    /** RN-04: la historia del paciente dueño de esta cuenta. Si no es un paciente o no tiene historia, se rechaza. */
    private HistoriaClinica historiaPropia(Usuario usuario) {
        autorizacion.exigir(usuario, Permiso.VER_MI_HISTORIAL);
        Paciente paciente = pacientes.findByUsuarioId(usuario.getId())
            .orElseThrow(() -> new AccesoDenegadoException("Tu cuenta no está vinculada a un paciente."));
        return historias.findByPacienteId(paciente.getId())
            .orElseThrow(() -> new RecursoNoEncontradoException("Aún no tienes una historia clínica abierta."));
    }
}
