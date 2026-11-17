package pe.edu.ucv.hce.persistencia;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ucv.hce.aplicacion.ClaveUtil;
import pe.edu.ucv.hce.dominio.CodigoRol;
import pe.edu.ucv.hce.dominio.Rol;
import pe.edu.ucv.hce.dominio.Usuario;

/**
 * Plan v3, item 0.2 (RF-20, RN-14, RN-16): el sistema arranca con un unico administrador de plataforma.
 * Si no existe ninguno, lo crea sin hospital y con clave temporal que debe cambiar en el primer ingreso (CU-18).
 * Usuario y clave temporal se configuran con hce.admin-inicial.usuario y hce.admin-inicial.clave (ver README).
 * Si ya hay un administrador de plataforma no hace nada: nunca pisa una clave ya cambiada.
 */
@Component
@Order(2)
public class AdministradorInicial implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(AdministradorInicial.class);

    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final String usuario;
    private final String claveTemporal;

    public AdministradorInicial(UsuarioRepository usuarios, RolRepository roles,
                                @Value("${hce.admin-inicial.usuario:admin}") String usuario,
                                @Value("${hce.admin-inicial.clave:Temporal2026}") String claveTemporal) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.usuario = usuario;
        this.claveTemporal = claveTemporal;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarios.existsByRolNombre(CodigoRol.ADMIN_PLATAFORMA.name())) {
            return;
        }
        String problema = ClaveUtil.validarPolitica(claveTemporal);
        if (problema != null) {
            throw new IllegalStateException("La clave temporal del administrador inicial no cumple la política: " + problema);
        }
        Rol rol = roles.findByNombre(CodigoRol.ADMIN_PLATAFORMA.name()).orElseThrow();
        Usuario admin = new Usuario(null, rol, usuario, ClaveUtil.bcrypt(claveTemporal), "ACTIVO");
        admin.setDebeCambiarClave(true);
        usuarios.save(admin);
        LOG.info("Administrador de plataforma inicial creado: usuario '{}'. Debe cambiar la clave temporal al entrar.", usuario);
    }
}
