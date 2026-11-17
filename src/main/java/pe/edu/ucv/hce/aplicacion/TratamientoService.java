package pe.edu.ucv.hce.aplicacion;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ucv.hce.dominio.Atencion;
import pe.edu.ucv.hce.dominio.Tratamiento;
import pe.edu.ucv.hce.dominio.Usuario;
import pe.edu.ucv.hce.persistencia.TratamientoRepository;

/**
 * CU-08 Registrar tratamiento e indicaciones (RF-08; RN-02, RN-03, RN-06, RN-07, RN-11). Responsable: Mattias (M-2),
 * corregido por Pablo para usar AutorizacionService, AtencionService y AuditoriaService.
 * Reglas: solo MEDICO (RN-02); la atencion debe ser del hospital del usuario (RN-06), propia (RN-11) y estar
 * ABIERTA (RN-03). Se audita (RN-07).
 */
@Service
public class TratamientoService {

    public static final String ACCION_TRATAMIENTO = "REGISTRAR_TRATAMIENTO";

    private final TratamientoRepository tratamientos;
    private final AtencionService atenciones;
    private final AutorizacionService autorizacion;
    private final AuditoriaService auditoria;

    public TratamientoService(TratamientoRepository tratamientos, AtencionService atenciones,
                              AutorizacionService autorizacion, AuditoriaService auditoria) {
        this.tratamientos = tratamientos;
        this.atenciones = atenciones;
        this.autorizacion = autorizacion;
        this.auditoria = auditoria;
    }

    /**
     * Agrega un tratamiento a una atencion ABIERTA del medico en sesion.
     * Errores: sin permiso o atencion ajena 403; otro hospital o inexistente 404; cerrada 409; campos vacios 400.
     */
    @Transactional
    public Tratamiento agregar(Usuario usuario, Integer idAtencion, String indicacion, String medicamento,
                               String dosis, String duracion, String ip) {
        autorizacion.exigir(usuario, Permiso.REGISTRAR_TRATAMIENTO);
        Atencion atencion = atenciones.atencionPropiaAbierta(usuario, idAtencion);
        exigir(indicacion, "la indicación");
        exigir(medicamento, "el medicamento");
        exigir(dosis, "la dosis");
        exigir(duracion, "la duración");
        Tratamiento t = tratamientos.save(new Tratamiento(atencion, indicacion.trim(), medicamento.trim(),
            dosis.trim(), duracion.trim()));
        auditoria.registrar(usuario, ACCION_TRATAMIENTO, "tratamiento", t.getId(), atencion.getHistoriaClinica(), ip);
        return t;
    }

    private static void exigir(String valor, String nombre) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Ingresa " + nombre + ".");
        }
    }
}
