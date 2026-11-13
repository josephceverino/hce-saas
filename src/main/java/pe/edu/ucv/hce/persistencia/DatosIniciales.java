package pe.edu.ucv.hce.persistencia;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pe.edu.ucv.hce.aplicacion.ClaveUtil;
import pe.edu.ucv.hce.dominio.Establecimiento;
import pe.edu.ucv.hce.dominio.Rol;
import pe.edu.ucv.hce.dominio.Usuario;

/** Crea datos de ejemplo para la demostracion: dos hospitales y un usuario en cada uno. */
@Component
public class DatosIniciales implements CommandLineRunner {

    private final EstablecimientoRepository establecimientos;
    private final RolRepository roles;
    private final UsuarioRepository usuarios;

    public DatosIniciales(EstablecimientoRepository establecimientos, RolRepository roles,
                          UsuarioRepository usuarios) {
        this.establecimientos = establecimientos;
        this.roles = roles;
        this.usuarios = usuarios;
    }

    @Override
    public void run(String... args) {
        if (establecimientos.count() > 0) {
            return;
        }
        Establecimiento a = establecimientos.save(
            new Establecimiento("Hospital Demo A", "0000001", "Lima", "ACTIVO"));
        Establecimiento b = establecimientos.save(
            new Establecimiento("Hospital Demo B", "0000002", "Lima", "ACTIVO"));
        Rol admision = roles.save(new Rol("Personal administrativo", "Registra y busca pacientes"));

        usuarios.save(new Usuario(a, admision, "admision.a", ClaveUtil.sha256("demo123"), "ACTIVO"));
        usuarios.save(new Usuario(b, admision, "admision.b", ClaveUtil.sha256("demo123"), "ACTIVO"));
    }
}
