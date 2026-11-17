package pe.edu.ucv.hce.persistencia;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ucv.hce.aplicacion.ClaveUtil;
import pe.edu.ucv.hce.dominio.CodigoRol;
import pe.edu.ucv.hce.dominio.Establecimiento;
import pe.edu.ucv.hce.dominio.ProfesionalSalud;
import pe.edu.ucv.hce.dominio.Rol;
import pe.edu.ucv.hce.dominio.Usuario;

/**
 * Datos iniciales (DEC-03). Siempre crea los 7 roles. Es idempotente: cada registro se crea solo si falta.
 * Plan v3, item 0.1: las cuentas de demostracion ya NO se siembran en la aplicacion. Solo se crean si
 * hce.demo-usuarios=true, que activan los tests como datos de prueba (src/test/resources/application.properties).
 * Cuentas de prueba (clave demo123): admision.a/b, medico.a/b, enfermera.a/b, admin.a y plataforma.
 */
@Component
@Order(1)
public class DatosIniciales implements CommandLineRunner {

    private static final String CLAVE_DEMO = "demo123";

    private final EstablecimientoRepository establecimientos;
    private final RolRepository roles;
    private final UsuarioRepository usuarios;
    private final ProfesionalSaludRepository profesionales;
    private final boolean usuariosDePrueba;

    public DatosIniciales(EstablecimientoRepository establecimientos, RolRepository roles,
                          UsuarioRepository usuarios, ProfesionalSaludRepository profesionales,
                          @Value("${hce.demo-usuarios:false}") boolean usuariosDePrueba) {
        this.usuariosDePrueba = usuariosDePrueba;
        this.establecimientos = establecimientos;
        this.roles = roles;
        this.usuarios = usuarios;
        this.profesionales = profesionales;
    }

    @Override
    @Transactional
    public void run(String... args) {
        for (CodigoRol codigo : CodigoRol.values()) {
            rol(codigo);
        }
        if (usuariosDePrueba) {
            sembrarUsuariosDePrueba();
        }
    }

    /** Solo para tests: dos hospitales y una cuenta de cada rol. Nunca en el paquete de la demo. */
    private void sembrarUsuariosDePrueba() {
        Establecimiento a = hospital("Hospital Demo A", "0000001");
        Establecimiento b = hospital("Hospital Demo B", "0000002");
        Rol admision = rol(CodigoRol.ADMISION);
        Rol medico = rol(CodigoRol.MEDICO);

        usuario(a, admision, "admision.a");
        usuario(b, admision, "admision.b");
        Usuario medicoA = usuario(a, medico, "medico.a");
        Usuario medicoB = usuario(b, medico, "medico.b");

        usuario(a, rol(CodigoRol.ENFERMERIA), "enfermera.a");
        usuario(b, rol(CodigoRol.ENFERMERIA), "enfermera.b");
        usuario(a, rol(CodigoRol.ADMIN_HOSPITAL), "admin.a");
        usuario(null, rol(CodigoRol.ADMIN_PLATAFORMA), "plataforma"); // RF-20: no pertenece a un hospital

        profesional(medicoA, "Médico de prueba", "Hospital A", "DEMO-0001");
        profesional(medicoB, "Médico de prueba", "Hospital B", "DEMO-0002");
    }

    private Establecimiento hospital(String nombre, String codigoIpress) {
        return establecimientos.findByCodigoIpress(codigoIpress)
            .orElseGet(() -> establecimientos.save(new Establecimiento(nombre, codigoIpress, "Lima", "ACTIVO")));
    }

    private Rol rol(CodigoRol codigo) {
        return roles.findByNombre(codigo.name())
            .orElseGet(() -> roles.save(new Rol(codigo.name(), codigo.descripcion())));
    }

    /** Crea el usuario si no existe; si existe con otro rol (base de la Fase 0), le asigna el rol correcto. */
    private Usuario usuario(Establecimiento hospital, Rol rol, String nombre) {
        Usuario u = usuarios.findByUsuario(nombre)
            .orElseGet(() -> usuarios.save(new Usuario(hospital, rol, nombre, ClaveUtil.bcrypt(CLAVE_DEMO), "ACTIVO")));
        if (!rol.getId().equals(u.getRol().getId())) {
            u.setRol(rol);
        }
        // RN-12: un usuario demo de una base anterior (SHA-256) pasa a BCrypt al arrancar
        if (ClaveUtil.necesitaMigrar(u.getContrasenaHash()) && ClaveUtil.coincide(CLAVE_DEMO, u.getContrasenaHash())) {
            u.actualizarContrasenaHash(ClaveUtil.bcrypt(CLAVE_DEMO));
        }
        return u;
    }

    private void profesional(Usuario usuario, String nombres, String apellidos, String colegiatura) {
        if (profesionales.findByUsuarioId(usuario.getId()).isEmpty()) {
            profesionales.save(new ProfesionalSalud(usuario, nombres, apellidos, colegiatura, "Medicina general", "MEDICO"));
        }
    }
}
