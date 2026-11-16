package pe.edu.ucv.hce.aplicacion;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ucv.hce.dominio.AccesoDenegadoException;
import pe.edu.ucv.hce.dominio.Atencion;
import pe.edu.ucv.hce.dominio.HistoriaClinica;
import pe.edu.ucv.hce.dominio.RecursoNoEncontradoException;
import pe.edu.ucv.hce.dominio.Usuario;
import pe.edu.ucv.hce.persistencia.AlergiaRepository;
import pe.edu.ucv.hce.persistencia.AntecedenteRepository;
import pe.edu.ucv.hce.persistencia.AtencionRepository;
import pe.edu.ucv.hce.persistencia.DiagnosticoRepository;
import pe.edu.ucv.hce.persistencia.EnmiendaRepository;
import pe.edu.ucv.hce.persistencia.HistoriaClinicaRepository;
import pe.edu.ucv.hce.persistencia.TratamientoRepository;

/**
 * CU-06 Consultar historia clinica (RF-06; RN-02, RN-06, RN-07).
 * Solo MEDICO y ENFERMERIA; solo historias del hospital del usuario; cada lectura se audita.
 */
@Service
public class HistorialService {

    public static final String ACCION_CONSULTAR = "CONSULTAR_HISTORIA";
    public static final String MENSAJE_OTRO_HOSPITAL =
        "Esta historia clínica pertenece a otro establecimiento. Para acceder a ella se requiere "
        + "un intercambio autorizado por el paciente (RN-06).";

    private final HistoriaClinicaRepository historias;
    private final AtencionRepository atenciones;
    private final DiagnosticoRepository diagnosticos;
    private final TratamientoRepository tratamientos;
    private final EnmiendaRepository enmiendas;
    private final AntecedenteRepository antecedentes;
    private final AlergiaRepository alergias;
    private final AutorizacionService autorizacion;
    private final AuditoriaService auditoria;

    public HistorialService(HistoriaClinicaRepository historias, AtencionRepository atenciones,
                            DiagnosticoRepository diagnosticos, TratamientoRepository tratamientos,
                            EnmiendaRepository enmiendas, AntecedenteRepository antecedentes,
                            AlergiaRepository alergias, AutorizacionService autorizacion,
                            AuditoriaService auditoria) {
        this.historias = historias;
        this.atenciones = atenciones;
        this.diagnosticos = diagnosticos;
        this.tratamientos = tratamientos;
        this.enmiendas = enmiendas;
        this.antecedentes = antecedentes;
        this.alergias = alergias;
        this.autorizacion = autorizacion;
        this.auditoria = auditoria;
    }

    /**
     * Carga la historia con sus atenciones, antecedentes y alergias.
     * Sin permiso: 403 (RN-02). No existe: 404. De otro hospital: 403 (RN-06). La lectura queda auditada (RN-07).
     */
    @Transactional
    public HistorialClinico consultar(Usuario usuario, Integer idHistoria, String ip) {
        autorizacion.exigir(usuario, Permiso.CONSULTAR_HISTORIA);
        HistoriaClinica historia = historias.findById(idHistoria)
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe la historia clínica pedida."));
        if (usuario.getEstablecimiento() == null
                || !historia.getEstablecimiento().getId().equals(usuario.getEstablecimiento().getId())) {
            throw new AccesoDenegadoException(MENSAJE_OTRO_HOSPITAL);
        }
        List<AtencionDetalle> detalle = atenciones.findAllByHistoriaClinicaIdOrderByFechaHoraDesc(historia.getId())
            .stream().map(this::armar).toList();
        HistorialClinico resultado = new HistorialClinico(historia,
            antecedentes.findAllByHistoriaClinicaIdOrderByFechaRegistroDesc(historia.getId()),
            alergias.findAllByHistoriaClinicaId(historia.getId()), detalle);
        auditoria.registrar(usuario, ACCION_CONSULTAR, "historia_clinica", historia.getId(), historia, ip);
        return resultado;
    }

    private AtencionDetalle armar(Atencion a) {
        return new AtencionDetalle(a, diagnosticos.findAllByAtencionIdOrderByIdAsc(a.getId()),
            tratamientos.findAllByAtencionIdOrderByIdAsc(a.getId()),
            enmiendas.findAllByAtencionIdOrderByFechaAsc(a.getId()));
    }
}
