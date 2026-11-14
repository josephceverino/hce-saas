package pe.edu.ucv.hce.persistencia;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ucv.hce.aplicacion.ClaveUtil;
import pe.edu.ucv.hce.dominio.CodigoRol;
import pe.edu.ucv.hce.dominio.Establecimiento;
import pe.edu.ucv.hce.dominio.ProfesionalSalud;
import pe.edu.ucv.hce.dominio.Rol;
import pe.edu.ucv.hce.dominio.Usuario;

/**
 * Datos ficticios de la demostracion (DEC-03). Es idempotente: cada registro se crea solo si falta,
 * asi una base ya existente en ./data recibe lo nuevo sin perder lo anterior.
 * Usuarios: admision.a, admision.b (ADMISION) y medico.a, medico.b (MEDICO). Clave: demo123.
 */
@Component
public class DatosIniciales implements CommandLineRunner {

    private static final String CLAVE_DEMO = "demo123";

    private final EstablecimientoRepository establecimientos;
    private final RolRepository roles;
    private final UsuarioRepository usuarios;
    private final ProfesionalSaludRepository profesionales;

    public DatosIniciales(EstablecimientoRepository establecimientos, RolRepository roles,
                          UsuarioRepository usuarios, ProfesionalSaludRepository profesionales) {
        this.establecimientos = establecimientos;
        this.roles = roles;
        this.usuarios = usuarios;
        this.profesionales = profesionales;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Establecimiento a = hospital("Hospital Demo A", "0000001");
        Establecimiento b = hospital("Hospital Demo B", "0000002");

        for (CodigoRol codigo : CodigoRol.values()) {
            rol(codigo);
        }
        Rol admision = rol(CodigoRol.ADMISION);
        Rol medico = rol(CodigoRol.MEDICO);

        usuario(a, admision, "admision.a");
        usuario(b, admision, "admision.b");
        Usuario medicoA = usuario(a, medico, "medico.a");
        Usuario medicoB = usuario(b, medico, "medico.b");

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
            .orElseGet(() -> usuarios.save(new Usuario(hospital, rol, nombre, ClaveUtil.sha256(CLAVE_DEMO), "ACTIVO")));
        if (!rol.getId().equals(u.getRol().getId())) {
            u.setRol(rol);
        }
        return u;
    }

    private void profesional(Usuario usuario, String nombres, String apellidos, String colegiatura) {
        if (profesionales.findByUsuarioId(usuario.getId()).isEmpty()) {
            profesionales.save(new ProfesionalSalud(usuario, nombres, apellidos, colegiatura, "Medicina general", "MEDICO"));
        }
    }
}
