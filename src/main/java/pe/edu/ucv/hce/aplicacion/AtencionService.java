package pe.edu.ucv.hce.aplicacion;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ucv.hce.dominio.AccesoDenegadoException;
import pe.edu.ucv.hce.dominio.Admision;
import pe.edu.ucv.hce.dominio.Atencion;
import pe.edu.ucv.hce.dominio.AtencionCerradaException;
import pe.edu.ucv.hce.dominio.Cie10;
import pe.edu.ucv.hce.dominio.Diagnostico;
import pe.edu.ucv.hce.dominio.DiagnosticoInvalidoException;
import pe.edu.ucv.hce.dominio.Enmienda;
import pe.edu.ucv.hce.dominio.HistoriaClinica;
import pe.edu.ucv.hce.dominio.ProfesionalSalud;
import pe.edu.ucv.hce.dominio.RecursoNoEncontradoException;
import pe.edu.ucv.hce.dominio.Usuario;
import pe.edu.ucv.hce.persistencia.AtencionRepository;
import pe.edu.ucv.hce.persistencia.DiagnosticoRepository;
import pe.edu.ucv.hce.persistencia.EnmiendaRepository;
import pe.edu.ucv.hce.persistencia.HistoriaClinicaRepository;
import pe.edu.ucv.hce.persistencia.ProfesionalSaludRepository;
import pe.edu.ucv.hce.persistencia.TratamientoRepository;

/**
 * CU-05 Registrar atencion medica (RF-05; RN-02, RN-03, RN-06, RN-07, RN-10, RN-11).
 * Solo MEDICO (RN-02). El profesional es el del usuario en sesion y el hospital el del usuario (RN-11, RN-06).
 * Una atencion cerrada es inmutable (RN-03): se corrige solo con enmienda.
 */
@Service
public class AtencionService {

    public static final String ACCION_CREAR = "CREAR_ATENCION";
    public static final String ACCION_DIAGNOSTICO = "AGREGAR_DIAGNOSTICO";
    public static final String ACCION_CERRAR = "CERRAR_ATENCION";
    public static final String ACCION_ENMENDAR = "ENMENDAR_ATENCION";
    public static final String ACCION_CONSULTAR = "CONSULTAR_ATENCION";

    public static final String MENSAJE_CERRADA =
        "La atención ya está cerrada y no se puede modificar. Registra una enmienda para corregirla (RN-03).";
    public static final String MENSAJE_NO_AUTOR = "Solo el médico que registró la atención puede modificarla.";

    private final HistoriaClinicaRepository historias;
    private final AtencionRepository atenciones;
    private final DiagnosticoRepository diagnosticos;
    private final TratamientoRepository tratamientos;
    private final EnmiendaRepository enmiendas;
    private final pe.edu.ucv.hce.persistencia.SolicitudExamenRepository examenes;
    private final ProfesionalSaludRepository profesionales;
    private final CatalogoCie10Service catalogo;
    private final AutorizacionService autorizacion;
    private final AuditoriaService auditoria;
    private final AdmisionService admisiones;

    public AtencionService(HistoriaClinicaRepository historias, AtencionRepository atenciones,
                           DiagnosticoRepository diagnosticos, TratamientoRepository tratamientos,
                           EnmiendaRepository enmiendas, pe.edu.ucv.hce.persistencia.SolicitudExamenRepository examenes, ProfesionalSaludRepository profesionales,
                           CatalogoCie10Service catalogo, AutorizacionService autorizacion,
                           AuditoriaService auditoria, AdmisionService admisiones) {
        this.admisiones = admisiones;
        this.historias = historias;
        this.atenciones = atenciones;
        this.diagnosticos = diagnosticos;
        this.tratamientos = tratamientos;
        this.enmiendas = enmiendas;
        this.examenes = examenes;
        this.profesionales = profesionales;
        this.catalogo = catalogo;
        this.autorizacion = autorizacion;
        this.auditoria = auditoria;
    }

    /** Crea una atencion ABIERTA en una historia del hospital del usuario (RN-06, RN-11). */
    @Transactional
    public AtencionDetalle crear(Usuario usuario, Integer idHistoria, String motivo, String anamnesis,
                                 String examenFisico, String ip) {
        autorizacion.exigir(usuario, Permiso.REGISTRAR_ATENCION);
        ProfesionalSalud profesional = profesionalDe(usuario);
        HistoriaClinica historia = historias.findById(idHistoria)
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe la historia clínica pedida."));
        if (!historia.getEstablecimiento().getId().equals(usuario.getEstablecimiento().getId())) {
            throw new AccesoDenegadoException(HistorialService.MENSAJE_OTRO_HOSPITAL);
        }
        Atencion atencion = atenciones.save(new Atencion(historia, profesional, usuario.getEstablecimiento(),
            LocalDateTime.now(), motivo.trim(), anamnesis.trim(), examenFisico.trim()));
        auditoria.registrar(usuario, ACCION_CREAR, "atencion", atencion.getId(), historia, ip);
        return armar(atencion);
    }

    /** Id de la atencion de una admision del hospital del medico (para continuar desde la cola), si ya se inicio. */
    @Transactional(readOnly = true)
    public java.util.Optional<Integer> idDeAdmision(Usuario usuario, Integer idAdmision) {
        autorizacion.exigir(usuario, Permiso.REGISTRAR_ATENCION);
        Admision admision = admisiones.delHospital(usuario, idAdmision); // RN-06
        return atenciones.findByAdmisionId(admision.getId()).map(Atencion::getId);
    }

    /** Datos clinicos ampliados de la atencion (CU-05 ampliado, D-40). Todos son de texto libre. */
    public record Ampliacion(String enfermedadActual, String funcionesBiologicas, String examenSistemas) { }

    /**
     * CU-05 ampliado (RN-13, RN-06, RN-03): el medico inicia la atencion de un paciente de la cola. La admision debe estar
     * LISTA PARA ATENCION (el dominio impone el orden), pasa a EN_ATENCION y la atencion queda ligada a ella.
     * El hospital sale de la sesion; una admision de otro hospital se trata como inexistente.
     */
    @Transactional
    public AtencionDetalle crearDesdeAdmision(Usuario usuario, Integer idAdmision, String motivo, String anamnesis,
                                              String examenFisico, Ampliacion ampliacion, String ip) {
        autorizacion.exigir(usuario, Permiso.REGISTRAR_ATENCION);
        Admision admision = admisiones.delHospital(usuario, idAdmision);
        if (atenciones.existsByAdmisionId(admision.getId())) {
            throw new IllegalArgumentException("Esa admisión ya tiene una atención registrada.");
        }
        admisiones.iniciarAtencion(usuario, idAdmision, ip); // RN-13: solo desde "listo para atencion"
        AtencionDetalle detalle = crear(usuario, admision.getHistoriaClinica().getId(), motivo, anamnesis, examenFisico, ip);
        Atencion atencion = detalle.atencion();
        atencion.ampliar(admision, ampliacion.enfermedadActual(), ampliacion.funcionesBiologicas(),
            ampliacion.examenSistemas());
        atenciones.save(atencion);
        return armar(atencion);
    }

    /**
     * Agrega un diagnostico a una atencion ABIERTA propia. El codigo debe estar en el catalogo CIE-10 (RN-10);
     * la descripcion se toma del catalogo, no de lo que escriba el cliente.
     */
    @Transactional
    public Diagnostico agregarDiagnostico(Usuario usuario, Integer idAtencion, String codigoCie10,
                                          String tipo, String ip) {
        autorizacion.exigir(usuario, Permiso.REGISTRAR_ATENCION);
        Atencion atencion = atencionPropia(usuario, idAtencion);
        exigirAbierta(atencion);
        if (!Diagnostico.PRINCIPAL.equals(tipo) && !Diagnostico.SECUNDARIO.equals(tipo)) {
            throw new IllegalArgumentException("El tipo de diagnóstico debe ser PRINCIPAL o SECUNDARIO.");
        }
        Cie10 entrada = catalogo.porCodigo(codigoCie10.trim())
            .orElseThrow(() -> new DiagnosticoInvalidoException(
                "El código " + codigoCie10.trim() + " no está en el catálogo CIE-10."));
        Diagnostico d = diagnosticos.save(new Diagnostico(atencion, entrada.codigo(), entrada.descripcion(), tipo));
        auditoria.registrar(usuario, ACCION_DIAGNOSTICO, "diagnostico", d.getId(), atencion.getHistoriaClinica(), ip);
        return d;
    }

    /** Cierra la atencion propia (RN-03). Exige al menos un diagnostico. Desde aqui ya no se modifica. */
    @Transactional
    public AtencionDetalle cerrar(Usuario usuario, Integer idAtencion, String ip) {
        autorizacion.exigir(usuario, Permiso.REGISTRAR_ATENCION);
        Atencion atencion = atencionPropia(usuario, idAtencion);
        exigirAbierta(atencion);
        if (diagnosticos.findAllByAtencionIdOrderByIdAsc(atencion.getId()).isEmpty()) {
            throw new IllegalArgumentException("Agrega al menos un diagnóstico antes de cerrar la atención.");
        }
        atencion.cerrar();
        atenciones.save(atencion);
        if (atencion.getAdmision() != null) {
            admisiones.cerrarAtencion(usuario, atencion.getAdmision().getId(), ip); // RN-13: EN_ATENCION -> ATENCION_CERRADA
        }
        auditoria.registrar(usuario, ACCION_CERRAR, "atencion", atencion.getId(), atencion.getHistoriaClinica(), ip);
        return armar(atencion);
    }

    /**
     * Enmienda en texto libre sobre una atencion CERRADA del hospital (RN-03). El autor es el medico en sesion
     * (la tabla enmienda guarda su propio profesional), asi que puede ser distinto del autor de la atencion.
     */
    @Transactional
    public Enmienda enmendar(Usuario usuario, Integer idAtencion, String motivo,
                             String contenidoCorregido, String ip) {
        autorizacion.exigir(usuario, Permiso.REGISTRAR_ATENCION);
        ProfesionalSalud autor = profesionalDe(usuario);
        Atencion atencion = atencionDelHospital(usuario, idAtencion);
        if (!atencion.estaCerrada()) {
            throw new IllegalArgumentException(
                "La atención sigue abierta: edítala directamente. Las enmiendas son solo para atenciones cerradas.");
        }
        Enmienda e = enmiendas.save(new Enmienda(atencion, autor, motivo.trim(), contenidoCorregido.trim(),
            LocalDateTime.now()));
        auditoria.registrar(usuario, ACCION_ENMENDAR, "enmienda", e.getId(), atencion.getHistoriaClinica(), ip);
        return e;
    }

    /** Una atencion con sus diagnosticos, tratamientos y enmiendas. MEDICO o ENFERMERIA, solo de su hospital. */
    @Transactional
    public AtencionDetalle detalle(Usuario usuario, Integer idAtencion, String ip) {
        autorizacion.exigir(usuario, Permiso.CONSULTAR_HISTORIA);
        Atencion atencion = atencionDelHospital(usuario, idAtencion);
        auditoria.registrar(usuario, ACCION_CONSULTAR, "atencion", atencion.getId(), atencion.getHistoriaClinica(), ip);
        return armar(atencion);
    }

    /**
     * Para CU-08 (RF-08; RN-03, RN-06, RN-11): la atencion debe ser del hospital del usuario, propia y estar ABIERTA.
     * Orden de los errores: otro hospital o inexistente 404, ajena 403, cerrada 409.
     */
    @Transactional
    public Atencion atencionPropiaAbierta(Usuario usuario, Integer idAtencion) {
        Atencion atencion = atencionPropia(usuario, idAtencion);
        exigirAbierta(atencion);
        return atencion;
    }

    /** RN-06: una atencion de otro hospital se trata como inexistente (no se revela que existe). */
    private Atencion atencionDelHospital(Usuario usuario, Integer idAtencion) {
        if (usuario.getEstablecimiento() == null) {
            throw new AccesoDenegadoException("Tu usuario no pertenece a un hospital.");
        }
        return atenciones.findByIdAndEstablecimientoId(idAtencion, usuario.getEstablecimiento().getId())
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe la atención pedida."));
    }

    /** RN-11: solo el profesional que registro la atencion la modifica. */
    private Atencion atencionPropia(Usuario usuario, Integer idAtencion) {
        Atencion atencion = atencionDelHospital(usuario, idAtencion);
        if (!atencion.getProfesional().getId().equals(profesionalDe(usuario).getId())) {
            throw new AccesoDenegadoException(MENSAJE_NO_AUTOR);
        }
        return atencion;
    }

    private void exigirAbierta(Atencion atencion) {
        if (atencion.estaCerrada()) {
            throw new AtencionCerradaException(MENSAJE_CERRADA);
        }
    }

    private ProfesionalSalud profesionalDe(Usuario usuario) {
        if (usuario.getEstablecimiento() == null) {
            throw new AccesoDenegadoException("Tu usuario no pertenece a un hospital.");
        }
        return profesionales.findByUsuarioId(usuario.getId())
            .orElseThrow(() -> new AccesoDenegadoException("Tu usuario no tiene un perfil de profesional de salud."));
    }

    private AtencionDetalle armar(Atencion a) {
        return new AtencionDetalle(a, diagnosticos.findAllByAtencionIdOrderByIdAsc(a.getId()),
            tratamientos.findAllByAtencionIdOrderByIdAsc(a.getId()),
            enmiendas.findAllByAtencionIdOrderByFechaAsc(a.getId()),
            examenes.findAllByAtencionIdOrderByIdAsc(a.getId()));
    }
}
