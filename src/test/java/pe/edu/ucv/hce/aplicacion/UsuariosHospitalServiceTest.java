package pe.edu.ucv.hce.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ucv.hce.aplicacion.UsuariosHospitalService.DatosUsuario;
import pe.edu.ucv.hce.dominio.AccesoDenegadoException;
import pe.edu.ucv.hce.dominio.CodigoRol;
import pe.edu.ucv.hce.dominio.NoAutenticadoException;
import pe.edu.ucv.hce.dominio.RecursoNoEncontradoException;
import pe.edu.ucv.hce.dominio.RegistroDuplicadoException;
import pe.edu.ucv.hce.dominio.Usuario;
import pe.edu.ucv.hce.persistencia.ProfesionalSaludRepository;
import pe.edu.ucv.hce.persistencia.RolRepository;
import pe.edu.ucv.hce.persistencia.UsuarioRepository;

/** CU-17 Gestionar usuarios del hospital (RF-11, RF-12; RN-16, RN-14, RN-07, RN-06). */
@SpringBootTest
@Transactional
class UsuariosHospitalServiceTest {

    private static final String IP = "127.0.0.1";

    @Autowired UsuariosHospitalService servicio;
    @Autowired AutenticacionService autenticacion;
    @Autowired UsuarioRepository usuarios;
    @Autowired RolRepository roles;
    @Autowired ProfesionalSaludRepository profesionales;

    Usuario adminA;
    Usuario adminB;

    @BeforeEach
    void escenario() {
        adminA = usuarios.findByUsuario("admin.a").orElseThrow();
        Usuario medicoB = usuarios.findByUsuario("medico.b").orElseThrow();
        adminB = usuarios.save(new Usuario(medicoB.getEstablecimiento(),
            roles.findByNombre(CodigoRol.ADMIN_HOSPITAL.name()).orElseThrow(), "admin.b.test", "hash", "ACTIVO"));
    }

    private DatosUsuario datos(String usuario, String rol, String colegiatura) {
        return new DatosUsuario("Lucía", "Torres Paredes", "40000011", "ltorres@example.com", usuario, rol, colegiatura, null);
    }

    /** CU-17: el usuario queda en el hospital del administrador, con clave temporal (RN-14) y como profesional. */
    @Test
    void creaUnMedicoEnSuHospitalConClaveTemporal() {
        var alta = servicio.crear(adminA, datos("ltorres", "MEDICO", "CMP-12345"), IP);
        Usuario u = usuarios.findByUsuario("ltorres").orElseThrow();
        assertEquals(adminA.getEstablecimiento().getId(), u.getEstablecimiento().getId());
        assertTrue(u.tieneRol(CodigoRol.MEDICO));
        assertTrue(u.isDebeCambiarClave());
        assertTrue(ClaveUtil.coincide(alta.claveTemporal(), u.getContrasenaHash()));
        assertEquals("CMP-12345", profesionales.findByUsuarioId(u.getId()).orElseThrow().getColegiatura());
        assertTrue(servicio.listar(adminA).stream().anyMatch(x -> x.getUsuario().equals("ltorres")));
    }

    /** RN-16: un administrador no crea ni ve usuarios de otro hospital, ni los desactiva. */
    @Test
    void unAdministradorNoTocaUsuariosDeOtroHospital() {
        var alta = servicio.crear(adminA, datos("ltorres", "ADMISION", null), IP);
        assertTrue(servicio.listar(adminB).stream().noneMatch(x -> x.getUsuario().equals("ltorres")));
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.desactivar(adminB, alta.usuario().getId(), IP));
        var deB = servicio.crear(adminB, datos("otro.b", "ENFERMERIA", "CEP-1"), IP);
        assertEquals(adminB.getEstablecimiento().getId(), deB.usuario().getEstablecimiento().getId(),
            "El hospital sale de la sesión del administrador, nunca del cliente");
    }

    /** RN-16: no puede crear administradores ni administradores de plataforma. */
    @Test
    void noCreaAdministradores() {
        assertThrows(AccesoDenegadoException.class, () -> servicio.crear(adminA, datos("x.admin", "ADMIN_PLATAFORMA", null), IP));
        assertThrows(AccesoDenegadoException.class, () -> servicio.crear(adminA, datos("x.admin2", "ADMIN_HOSPITAL", null), IP));
        assertThrows(IllegalArgumentException.class, () -> servicio.crear(adminA, datos("x.rol", "INVENTADO", null), IP));
    }

    /** RN-02: solo el administrador del hospital gestiona usuarios. */
    @Test
    void elPersonalClinicoNoCreaUsuarios() {
        Usuario medico = usuarios.findByUsuario("medico.a").orElseThrow();
        assertThrows(AccesoDenegadoException.class, () -> servicio.crear(medico, datos("x.uno", "ADMISION", null), IP));
        Usuario plataforma = usuarios.findByUsuario("plataforma").orElseThrow();
        assertThrows(AccesoDenegadoException.class, () -> servicio.crear(plataforma, datos("x.dos", "ADMISION", null), IP));
    }

    @Test
    void elPersonalClinicoNecesitaColegiaturaYElUsuarioEsUnico() {
        assertThrows(IllegalArgumentException.class, () -> servicio.crear(adminA, datos("sin.coleg", "MEDICO", " "), IP));
        assertThrows(RegistroDuplicadoException.class, () -> servicio.crear(adminA, datos("medico.a", "ADMISION", null), IP));
    }

    /** CU-17 paso 4: una cuenta desactivada no puede iniciar sesion; al reactivarla vuelve a entrar. */
    @Test
    void desactivarYReactivar() {
        var alta = servicio.crear(adminA, datos("ltorres", "ADMISION", null), IP);
        servicio.desactivar(adminA, alta.usuario().getId(), IP);
        assertThrows(NoAutenticadoException.class, () -> autenticacion.iniciarSesion("ltorres", alta.claveTemporal()));
        servicio.reactivar(adminA, alta.usuario().getId(), IP);
        assertEquals("ltorres", autenticacion.iniciarSesion("ltorres", alta.claveTemporal()).getUsuario());
        assertThrows(IllegalArgumentException.class, () -> servicio.desactivar(adminA, adminA.getId(), IP));
    }
}
