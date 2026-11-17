package pe.edu.ucv.hce.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ucv.hce.dominio.CodigoRol;
import pe.edu.ucv.hce.dominio.Usuario;
import pe.edu.ucv.hce.persistencia.RolRepository;
import pe.edu.ucv.hce.persistencia.UsuarioRepository;

/**
 * Menu por rol (docs/diseno_ui.md, DEC-03, RN-02) y usuarios demo de cada rol (RF-11, RF-19, RF-20 en v5).
 */
@SpringBootTest
@Transactional
class MenuServiceTest {

    @Autowired MenuService menu;
    @Autowired UsuarioRepository usuarios;
    @Autowired RolRepository roles;

    private List<String> claves(String usuario) {
        Usuario u = usuarios.findByUsuario(usuario).orElseThrow();
        return menu.menuPara(u).stream().map(ItemMenu::clave).toList();
    }

    @Test
    void admisionRegistraYBuscaPacientes() {
        assertEquals(List.of("inicio", "pacientes", "registrar-paciente", "perfil"), claves("admision.a"));
    }

    @Test
    void medicoAtiendeYVeHistorias() {
        assertEquals(List.of("inicio", "pacientes", "historia", "atencion", "perfil"), claves("medico.a"));
    }

    /** RN-02: enfermeria ve historias pero no tiene "Registrar atencion". */
    @Test
    void enfermeriaNoTieneRegistrarAtencion() {
        List<String> claves = claves("enfermera.a");
        assertTrue(claves.contains("historia"));
        assertFalse(claves.contains("atencion"));
    }

    @Test
    void adminDelHospitalVeTableroYAuditoria() {
        assertEquals(List.of("inicio", "auditoria", "perfil"), claves("admin.a"));
    }

    @Test
    void adminDePlataformaSoloVeHospitales() {
        assertEquals(List.of("hospitales", "perfil"), claves("plataforma"));
    }

    /** El admin de plataforma no pertenece a ningun hospital; los demas usuarios demo si. */
    @Test
    void usuariosDemoYSuHospital() {
        assertNull(usuarios.findByUsuario("plataforma").orElseThrow().getEstablecimiento());
        assertNotNull(usuarios.findByUsuario("admin.a").orElseThrow().getEstablecimiento());
        assertNotNull(usuarios.findByUsuario("enfermera.b").orElseThrow().getEstablecimiento());
    }

    /** Nadie ve opciones fuera del alcance (calendario, citas, chat, facturacion, etc.). */
    @Test
    void ningunMenuTrae_opcionesFueraDelAlcance() {
        for (String u : List.of("admision.a", "medico.a", "enfermera.a", "admin.a", "plataforma")) {
            for (String clave : claves(u)) {
                assertFalse(List.of("calendario", "citas", "chat", "correo", "kanban", "facturacion", "telemedicina")
                    .contains(clave));
            }
        }
    }

    /** Plan v3, 0.4: los 7 roles tienen menu propio; apoyo al diagnostico tiene su Inicio. */
    @Test
    void apoyoAlDiagnosticoTieneInicioPropio() {
        Usuario apoyo = usuarios.save(new Usuario(usuarios.findByUsuario("admin.a").orElseThrow().getEstablecimiento(),
            roles.findByNombre(CodigoRol.APOYO_DIAGNOSTICO.name()).orElseThrow(), "apoyo.menu.test", "hash", "ACTIVO"));
        assertEquals(List.of("inicio", "perfil"), menu.menuPara(apoyo).stream().map(ItemMenu::clave).toList());
    }
}
