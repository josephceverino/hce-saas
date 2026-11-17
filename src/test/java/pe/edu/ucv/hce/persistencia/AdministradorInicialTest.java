package pe.edu.ucv.hce.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pe.edu.ucv.hce.aplicacion.ClaveUtil;
import pe.edu.ucv.hce.dominio.CodigoRol;
import pe.edu.ucv.hce.dominio.Usuario;

/**
 * Plan v3, 0.2 (RF-20, RN-14, RN-16): sin cuentas de prueba, el sistema arranca con un unico administrador de
 * plataforma, sin hospital, con clave temporal que debe cambiar. Base propia en memoria.
 */
@SpringBootTest(properties = {"hce.demo-usuarios=false", "spring.datasource.url=jdbc:h2:mem:admin-inicial-test",
    "hce.admin-inicial.usuario=admin.prueba", "hce.admin-inicial.clave=Inicial123"})
class AdministradorInicialTest {

    @Autowired UsuarioRepository usuarios;
    @Autowired AdministradorInicial inicial;

    @Test
    void arrancaConUnUnicoAdministradorDePlataforma() {
        List<Usuario> todos = usuarios.findAll();
        assertEquals(1, todos.size(), "Sin cuentas de demostración solo existe el administrador inicial");
        Usuario admin = todos.get(0);
        assertEquals("admin.prueba", admin.getUsuario());
        assertTrue(admin.tieneRol(CodigoRol.ADMIN_PLATAFORMA));
        assertNull(admin.getEstablecimiento());
        assertTrue(admin.isDebeCambiarClave(), "RN-14: la clave es temporal");
        assertTrue(ClaveUtil.coincide("Inicial123", admin.getContrasenaHash()));
    }

    @Test
    void noSeDuplicaAlReiniciar() {
        inicial.run();
        assertEquals(1, usuarios.findAll().size());
    }
}
