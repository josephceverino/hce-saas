package pe.edu.ucv.hce.dominio;

/**
 * Codigos de rol (DEC-03, docs/roles_permisos.md). Se guardan en rol.nombre;
 * la descripcion legible va en rol.descripcion (la tabla rol no tiene columna codigo).
 */
public enum CodigoRol {
    PACIENTE("Paciente o representante legal"),          // A-01
    MEDICO("Médico"),                                     // A-02
    ENFERMERIA("Enfermería"),                             // A-03
    ADMISION("Admisión y archivo"),                       // A-04
    APOYO_DIAGNOSTICO("Apoyo al diagnóstico"),            // A-05
    ADMIN_HOSPITAL("Administrador del hospital"),         // A-06
    ADMIN_PLATAFORMA("Administrador de plataforma SaaS"); // A-07

    private final String descripcion;

    CodigoRol(String descripcion) {
        this.descripcion = descripcion;
    }

    public String descripcion() {
        return descripcion;
    }
}
