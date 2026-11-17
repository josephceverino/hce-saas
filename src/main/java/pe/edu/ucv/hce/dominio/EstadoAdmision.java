package pe.edu.ucv.hce.dominio;

import java.util.EnumSet;
import java.util.Set;

/**
 * RN-13: estados de una admision y su unico orden permitido.
 * ADMITIDO -> EN_ENFERMERIA -> LISTO_PARA_ATENCION -> EN_ATENCION -> ATENCION_CERRADA; CANCELADO solo desde ADMITIDO.
 * La regla vive aqui (dominio), no en el controlador ni en el JS.
 */
public enum EstadoAdmision {
    ADMITIDO("Admitido"),
    EN_ENFERMERIA("En enfermería"),
    LISTO_PARA_ATENCION("Listo para atención"),
    EN_ATENCION("En atención"),
    ATENCION_CERRADA("Atención cerrada"),
    CANCELADO("Cancelado");

    private final String descripcion;

    EstadoAdmision(String descripcion) {
        this.descripcion = descripcion;
    }

    public String descripcion() { return descripcion; }

    /** Estados a los que se puede pasar desde este (RN-13). */
    public Set<EstadoAdmision> siguientes() {
        return switch (this) {
            case ADMITIDO -> EnumSet.of(EN_ENFERMERIA, CANCELADO);
            case EN_ENFERMERIA -> EnumSet.of(LISTO_PARA_ATENCION);
            case LISTO_PARA_ATENCION -> EnumSet.of(EN_ATENCION);
            case EN_ATENCION -> EnumSet.of(ATENCION_CERRADA);
            case ATENCION_CERRADA, CANCELADO -> EnumSet.noneOf(EstadoAdmision.class);
        };
    }

    public boolean puedePasarA(EstadoAdmision destino) {
        return siguientes().contains(destino);
    }

    /** Una admision activa todavia esta en el hospital (no termino ni se cancelo). */
    public boolean esActiva() {
        return this != ATENCION_CERRADA && this != CANCELADO;
    }
}
