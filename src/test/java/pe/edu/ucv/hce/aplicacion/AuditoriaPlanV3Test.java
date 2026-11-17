package pe.edu.ucv.hce.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ucv.hce.aplicacion.AtencionService.Ampliacion;
import pe.edu.ucv.hce.aplicacion.EnfermeriaService.DatosSignos;
import pe.edu.ucv.hce.dominio.Admision;
import pe.edu.ucv.hce.dominio.CodigoRol;
import pe.edu.ucv.hce.dominio.Paciente;
import pe.edu.ucv.hce.dominio.ProfesionalSalud;
import pe.edu.ucv.hce.dominio.Usuario;
import pe.edu.ucv.hce.persistencia.ProfesionalSaludRepository;
import pe.edu.ucv.hce.persistencia.RolRepository;
import pe.edu.ucv.hce.persistencia.UsuarioRepository;

/**
 * R4.4, CU-25 (RF-13; RN-07, RN-06): la auditoria registra las acciones del plan v3 (admision, atencion desde la cola,
 * examenes, restablecer clave) y cada administrador de hospital ve solo lo de su hospital.
 */
@SpringBootTest
@Transactional
class AuditoriaPlanV3Test {

    private static final String IP = "127.0.0.1";

    @Autowired AuditoriaService auditoria;
    @Autowired AtencionService atenciones;
    @Autowired AdmisionService admisiones;
    @Autowired EnfermeriaService enfermeria;
    @Autowired ExamenService examenes;
    @Autowired RestablecimientoClaveService restablecimiento;
    @Autowired RegistroPacienteService registro;
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
            roles.findByNombre(CodigoRol.ADMIN_HOSPITAL.name()).orElseThrow(), "admin.b.audit", "hash", "ACTIVO"));
        Usuario admision = usuarios.findByUsuario("admision.a").orElseThrow();
        Usuario enfermera = usuarios.findByUsuario("enfermera.a").orElseThrow();
        Usuario medico = usuarios.findByUsuario("medico.a").orElseThrow();
        Usuario apoyo = usuarios.save(new Usuario(medico.getEstablecimiento(),
            roles.findByNombre(CodigoRol.APOYO_DIAGNOSTICO.name()).orElseThrow(), "apoyo.audit.test", "hash", "ACTIVO"));
        profesionales.save(new ProfesionalSalud(apoyo, "Apoyo", "Auditoria", "TM-7", "Laboratorio", "APOYO_DIAGNOSTICO"));

        var historia = registro.registrarPacienteYAbrirHistoria(new Paciente("DNI", "70000131", "Paciente de prueba",
            "Auditoria", LocalDate.of(1990, 1, 1), "F", "999111222", "prueba@example.com", "Calle Falsa 123"),
            admision, IP).historia();
        Admision a = admisiones.admitir(admision, historia.getId(), Admision.CONSULTA_EXTERNA, IP);
        admisiones.tomarEnEnfermeria(enfermera, a.getId(), IP);
        enfermeria.registrarSignos(enfermera, a.getId(), new DatosSignos(120, 80, 72, 16, new BigDecimal("36.6"), 98), IP);
        admisiones.marcarListo(enfermera, a.getId(), IP);
        Integer id = atenciones.crearDesdeAdmision(medico, a.getId(), "Dolor", "A", "E", new Ampliacion(null, null, null), IP)
            .atencion().getId();
        var s = examenes.solicitar(medico, id, "Hemograma", null, IP);
        examenes.registrarResultado(apoyo, s.getId(), "Normal", IP);
        restablecimiento.restablecerPaciente(admision, historia.getId(), IP);
    }

    private long total(Usuario admin, String accion) {
        return auditoria.consultar(admin, new FiltroAuditoria(null, accion, null, null, null), 0, 50).total();
    }

    /** RN-07: cada accion nueva deja su registro y se puede filtrar. */
    @Test
    void lasAccionesNuevasQuedanRegistradasYFiltrables() {
        var acciones = auditoria.accionesDelHospital(adminA);
        for (String esperada : new String[] {"ADMITIR_PACIENTE", "CAMBIAR_ESTADO_ADMISION:EN_ATENCION", "SOLICITAR_EXAMEN",
                "REGISTRAR_RESULTADO_EXAMEN", "RESTABLECER_CLAVE", "REGISTRAR_SIGNOS_VITALES"}) {
            assertTrue(acciones.contains(esperada), "Falta en el filtro: " + esperada);
            assertTrue(total(adminA, esperada) >= 1, "Sin registros de " + esperada);
        }
    }

    /** RN-06: el administrador de otro hospital no ve nada de lo anterior. */
    @Test
    void otroHospitalNoVeEsosRegistros() {
        for (String accion : new String[] {"ADMITIR_PACIENTE", "SOLICITAR_EXAMEN", "REGISTRAR_RESULTADO_EXAMEN",
                "RESTABLECER_CLAVE"}) {
            assertEquals(0, total(adminB, accion), "RN-06: no debe ver " + accion);
        }
        assertFalse(auditoria.accionesDelHospital(adminB).contains("SOLICITAR_EXAMEN"));
    }
}
