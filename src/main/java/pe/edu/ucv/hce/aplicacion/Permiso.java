package pe.edu.ucv.hce.aplicacion;

import static pe.edu.ucv.hce.dominio.CodigoRol.*;

import java.util.EnumSet;
import java.util.Set;
import pe.edu.ucv.hce.dominio.CodigoRol;

/**
 * Permisos por rol (DEC-03, RF-12). Matriz tomada de docs/roles_permisos.md (columna "Actor" de los RF).
 * v3: constantes en codigo. v5: se migra a la tabla rol_permiso.
 */
public enum Permiso {
    REGISTRAR_PACIENTE("RF-01", ADMISION),
    BUSCAR_PACIENTE("RF-02", MEDICO, ENFERMERIA, ADMISION),
    ACTUALIZAR_PACIENTE("RF-03", ADMISION),
    ABRIR_HISTORIA("RF-04", ADMISION),
    REGISTRAR_ATENCION("RF-05", MEDICO),
    CONSULTAR_HISTORIA("RF-06", MEDICO, ENFERMERIA),
    REGISTRAR_ANTECEDENTES_ALERGIAS("RF-07", MEDICO, ENFERMERIA),
    REGISTRAR_TRATAMIENTO("RF-08", MEDICO),
    REGISTRAR_RESULTADO_EXAMEN("RF-09", APOYO_DIAGNOSTICO),
    GESTIONAR_USUARIOS("RF-11", ADMIN_HOSPITAL),
    GESTIONAR_PERMISOS("RF-12", ADMIN_HOSPITAL),
    VER_MI_HISTORIAL("RF-14, RF-15, RF-16", PACIENTE),
    GESTIONAR_CONSENTIMIENTO("RF-17", PACIENTE),
    INTERCAMBIAR_INFORMACION("RF-18", MEDICO),
    VER_REPORTES("RF-19", ADMIN_HOSPITAL),
    GESTIONAR_HOSPITALES("RF-20", ADMIN_PLATAFORMA);

    private final String requisito;
    private final Set<CodigoRol> roles;

    Permiso(String requisito, CodigoRol primero, CodigoRol... resto) {
        this.requisito = requisito;
        this.roles = EnumSet.of(primero, resto);
    }

    public String requisito() { return requisito; }

    public boolean permite(CodigoRol rol) { return roles.contains(rol); }
}
