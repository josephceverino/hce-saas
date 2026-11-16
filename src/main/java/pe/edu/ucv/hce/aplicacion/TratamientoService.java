package pe.edu.ucv.hce.aplicacion;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ucv.hce.dominio.Atencion;
import pe.edu.ucv.hce.dominio.Tratamiento;
import pe.edu.ucv.hce.dominio.Usuario;
import pe.edu.ucv.hce.dominio.AccesoDenegadoException;
import pe.edu.ucv.hce.dominio.AtencionCerradaException;
import pe.edu.ucv.hce.dominio.RecursoNoEncontradoException;
import pe.edu.ucv.hce.persistencia.AtencionRepository;
import pe.edu.ucv.hce.persistencia.TratamientoRepository;
import pe.edu.ucv.hce.persistencia.RegistroAuditoriaRepository;
import pe.edu.ucv.hce.dominio.RegistroAuditoria;

import java.time.LocalDateTime;

@Service
public class TratamientoService {

    private final TratamientoRepository tratamientoRepository;
    private final AtencionRepository atencionRepository;
    private final RegistroAuditoriaRepository auditoriaRepository;

    public TratamientoService(TratamientoRepository tratamientoRepository,
                              AtencionRepository atencionRepository,
                              RegistroAuditoriaRepository auditoriaRepository) {
        this.tratamientoRepository = tratamientoRepository;
        this.atencionRepository = atencionRepository;
        this.auditoriaRepository = auditoriaRepository;
    }

    /**
     * CU-08 Registrar tratamiento e indicaciones (RF-08; RN-02, RN-03, RN-11). Responsable: Mattias (tarea M-2).
     */
    @Transactional
    public Tratamiento agregar(Usuario usuario, Integer idAtencion, String indicacion, String medicamento, String dosis, String duracion, String ip) {
        // Validar campos obligatorios vacíos o nulos
        if (indicacion == null || indicacion.trim().isEmpty() ||
            medicamento == null || medicamento.trim().isEmpty() ||
            dosis == null || dosis.trim().isEmpty() ||
            duracion == null || duracion.trim().isEmpty()) {
            throw new IllegalArgumentException("Los campos obligatorios no pueden estar vacíos.");
        }

        // Validar Permiso.REGISTRAR_TRATAMIENTO (RN-02: solo médico)
        if (usuario.getRol() == null || !"MEDICO".equalsIgnoreCase(usuario.getRol().getNombre())) {
            throw new AccesoDenegadoException("Acceso denegado: solo el médico puede registrar tratamientos.");
        }

        Atencion atencion = atencionRepository.findById(idAtencion)
                .orElseThrow(() -> new RecursoNoEncontradoException("Atención no encontrada o inexistente."));

        // Validar atención cerrada (RN-03)
        if ("CERRADA".equalsIgnoreCase(atencion.getEstado())) {
            throw new AtencionCerradaException("No se puede agregar tratamiento a una atención cerrada.");
        }

        // Validar establecimiento / hospital (RN-06)
        if (atencion.getEstablecimiento() != null && usuario.getEstablecimiento() != null) {
            if (!atencion.getEstablecimiento().getId().equals(usuario.getEstablecimiento().getId())) {
                throw new RecursoNoEncontradoException("Atención de otro hospital.");
            }
        }

        // Crear y guardar el tratamiento usando el constructor exacto de la entidad
        Tratamiento tratamiento = new Tratamiento(
            atencion,
            indicacion,
            medicamento,
            dosis,
            duracion
        );

        Tratamiento guardado = tratamientoRepository.save(tratamiento);

        // Registrar auditoría (RN-07)
        RegistroAuditoria auditoria = new RegistroAuditoria(
            usuario,
            atencion.getHistoriaClinica(),
            LocalDateTime.now(),
            "REGISTRAR_TRATAMIENTO",
            "TRATAMIENTO",
            guardado.getId(),
            ip != null ? ip : "127.0.0.1"
        );
        auditoriaRepository.save(auditoria);

        return guardado;
    }
}