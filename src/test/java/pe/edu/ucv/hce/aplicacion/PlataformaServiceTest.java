package pe.edu.ucv.hce.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ucv.hce.aplicacion.PlataformaService.DatosHospital;
import pe.edu.ucv.hce.dominio.AccesoDenegadoException;
import pe.edu.ucv.hce.dominio.CodigoRol;
import pe.edu.ucv.hce.dominio.RegistroDuplicadoException;
import pe.edu.ucv.hce.dominio.Usuario;
import pe.edu.ucv.hce.persistencia.EstablecimientoRepository;
import pe.edu.ucv.hce.persistencia.RegistroAuditoriaRepository;
import pe.edu.ucv.hce.persistencia.UsuarioRepository;

/** CU-16 Dar de alta un hospital (RF-20; RN-16, RN-14, RN-07). */
@SpringBootTest
@Transactional
class PlataformaServiceTest {

    private static final String IP = "127.0.0.1";

    @Autowired PlataformaService plataforma;
    @Autowired IndicadoresService indicadores;
    @Autowired UsuarioRepository usuarios;
    @Autowired EstablecimientoRepository establecimientos;
    @Autowired RegistroAuditoriaRepository auditoria;

    private Usuario u(String nombre) {
        return usuarios.findByUsuario(nombre).orElseThrow();
    }

    private DatosHospital datos(String codigo, String usuario) {
        return new DatosHospital("Hospital Regional de Prueba", codigo, "La Libertad", "ESTANDAR",
            "Administradora de Prueba", "40000001", "admin@example.com", usuario);
    }

    /** CU-16: crea hospital, suscripcion y administrador con clave temporal que debe cambiar (RN-14). */
    @Test
    void elAdministradorDePlataformaDaDeAltaUnHospital() {
        var alta = plataforma.altaHospital(u("plataforma"), datos("1234567", "admin.regional"), IP);
        assertEquals("1234567", alta.hospital().getCodigoIpress());
        assertEquals("ESTANDAR", alta.suscripcion().getPlan());
        Usuario admin = u("admin.regional");
        assertTrue(admin.tieneRol(CodigoRol.ADMIN_HOSPITAL));
        assertEquals(alta.hospital().getId(), admin.getEstablecimiento().getId());
        assertTrue(admin.isDebeCambiarClave(), "RN-14: clave temporal");
        assertTrue(ClaveUtil.coincide(alta.claveTemporal(), admin.getContrasenaHash()));
        assertFalse(admin.getContrasenaHash().contains(alta.claveTemporal()), "Solo se guarda el hash");
        assertEquals(null, ClaveUtil.validarPolitica(alta.claveTemporal()), "La temporal cumple la política");
        assertTrue(auditoria.findAll().stream().anyMatch(r -> PlataformaService.ACCION_ALTA_HOSPITAL.equals(r.getAccion())));
        assertTrue(indicadores.hospitales(u("plataforma")).stream()
            .anyMatch(h -> "1234567".equals(h.codigoIpress()) && "ESTANDAR".equals(h.plan())));
    }

    /** RN-16: ni el administrador del hospital ni el personal clinico crean hospitales. */
    @Test
    void soloElAdministradorDePlataformaCreaHospitales() {
        long antes = establecimientos.count();
        assertThrows(AccesoDenegadoException.class, () -> plataforma.altaHospital(u("admin.a"), datos("7654321", "x.uno"), IP));
        assertThrows(AccesoDenegadoException.class, () -> plataforma.altaHospital(u("medico.a"), datos("7654322", "x.dos"), IP));
        assertEquals(antes, establecimientos.count());
    }

    /** CU-16 alternativo: el codigo del establecimiento ya existe. */
    @Test
    void rechazaUnCodigoDeEstablecimientoRepetido() {
        RegistroDuplicadoException e = assertThrows(RegistroDuplicadoException.class,
            () -> plataforma.altaHospital(u("plataforma"), datos("0000001", "admin.nuevo"), IP));
        assertEquals("Ya existe un hospital con el código 0000001.", e.getMessage());
    }

    @Test
    void rechazaUnUsuarioRepetidoYUnPlanInvalido() {
        assertThrows(RegistroDuplicadoException.class,
            () -> plataforma.altaHospital(u("plataforma"), datos("2222222", "medico.a"), IP));
        assertThrows(IllegalArgumentException.class, () -> plataforma.altaHospital(u("plataforma"),
            new DatosHospital("H", "3333333", "Lima", "GRATIS", "N", "40000002", "a@example.com", "otro.admin"), IP));
    }

    @Test
    void laClaveTemporalCambiaCadaVez() {
        assertFalse(ClaveUtil.generarTemporal().equals(ClaveUtil.generarTemporal()));
    }
}
