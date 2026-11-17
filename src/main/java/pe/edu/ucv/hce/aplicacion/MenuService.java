package pe.edu.ucv.hce.aplicacion;

import java.util.List;
import org.springframework.stereotype.Service;
import pe.edu.ucv.hce.dominio.CodigoRol;
import pe.edu.ucv.hce.dominio.Usuario;

/**
 * Menu por rol (docs/diseno_ui.md, "Mapa de pantallas"; DEC-03, RN-02).
 * El menu se decide aqui, no en el JS: el frontend solo dibuja lo que recibe en /api/sesion.
 * Solo aparecen pantallas del alcance (sin calendario, citas, chat ni facturacion).
 * Ocultar una opcion no reemplaza la autorizacion: cada servicio sigue exigiendo su permiso.
 */
@Service
public class MenuService {

    private static final ItemMenu INICIO = new ItemMenu("inicio", "Inicio", "/inicio/");
    /** RF-22 (plan v3): cola del dia. Admision la ve como "Pacientes del dia"; enfermeria y medico como su cola. */
    private static final ItemMenu COLA_ADMISION = new ItemMenu("cola", "Pacientes del día", "/cola/");
    private static final ItemMenu COLA_CLINICA = new ItemMenu("cola", "Cola de pacientes", "/cola/");
    private static final ItemMenu PACIENTES = new ItemMenu("pacientes", "Pacientes", "/pacientes/");
    private static final ItemMenu REGISTRAR_PACIENTE =
        new ItemMenu("registrar-paciente", "Registrar paciente", "/pacientes/nuevo.html");
    private static final ItemMenu HISTORIA = new ItemMenu("historia", "Historia clínica", "/historia/");
    private static final ItemMenu ATENCION = new ItemMenu("atencion", "Registrar atención", "/atencion/");
    private static final ItemMenu MI_HISTORIAL = new ItemMenu("mi-historial", "Mi historial", "/mi-historial/");
    private static final ItemMenu CONSENTIMIENTO =
        new ItemMenu("consentimiento", "Consentimiento", "/consentimiento/");
    /** CU-23: bandeja de examenes de apoyo al diagnostico. */
    private static final ItemMenu EXAMENES = new ItemMenu("examenes", "Exámenes", "/examenes/");
    private static final ItemMenu USUARIOS = new ItemMenu("usuarios", "Usuarios", "/usuarios/");
    private static final ItemMenu AUDITORIA = new ItemMenu("auditoria", "Auditoría", "/auditoria/");
    private static final ItemMenu HOSPITALES = new ItemMenu("hospitales", "Hospitales", "/hospitales/");
    private static final ItemMenu PERFIL = new ItemMenu("perfil", "Mi perfil", "/perfil/");

    /** Opciones del usuario, en el orden en que se muestran. "Mi perfil" va para todos al final. */
    public List<ItemMenu> menuPara(Usuario usuario) {
        return List.of(opcionesDe(usuario), List.of(PERFIL)).stream().flatMap(List::stream).toList();
    }

    private List<ItemMenu> opcionesDe(Usuario usuario) {
        if (usuario.tieneRol(CodigoRol.ADMISION)) {
            return List.of(INICIO, COLA_ADMISION, PACIENTES, REGISTRAR_PACIENTE);
        }
        if (usuario.tieneRol(CodigoRol.MEDICO)) {
            return List.of(INICIO, COLA_CLINICA, PACIENTES, HISTORIA, ATENCION);
        }
        if (usuario.tieneRol(CodigoRol.ENFERMERIA)) {
            return List.of(INICIO, COLA_CLINICA, PACIENTES, HISTORIA);
        }
        if (usuario.tieneRol(CodigoRol.PACIENTE)) {
            return List.of(MI_HISTORIAL, CONSENTIMIENTO);
        }
        if (usuario.tieneRol(CodigoRol.ADMIN_HOSPITAL)) {
            return List.of(INICIO, USUARIOS, AUDITORIA);
        }
        if (usuario.tieneRol(CodigoRol.ADMIN_PLATAFORMA)) {
            return List.of(HOSPITALES);
        }
        // APOYO_DIAGNOSTICO (A-05): Inicio y la bandeja de examenes solicitados (CU-23)
        return List.of(INICIO, EXAMENES);
    }
}
