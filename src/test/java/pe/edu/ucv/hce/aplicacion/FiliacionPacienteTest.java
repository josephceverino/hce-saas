package pe.edu.ucv.hce.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ucv.hce.dominio.Paciente;
import pe.edu.ucv.hce.dominio.Usuario;
import pe.edu.ucv.hce.persistencia.PacienteRepository;
import pe.edu.ucv.hce.persistencia.UsuarioRepository;

/** Plan v3, 1.3 (RF-01, CU-03): la filiacion completa se guarda con el registro del paciente. */
@SpringBootTest
@Transactional
class FiliacionPacienteTest {

    @Autowired RegistroPacienteService registro;
    @Autowired UsuarioRepository usuarios;
    @Autowired PacienteRepository pacientes;

    @Test
    void laFiliacionCompletaSeGuarda() {
        Usuario admision = usuarios.findByUsuario("admision.a").orElseThrow();
        Paciente p = new Paciente("DNI", "70000051", "Paciente de prueba", "Filiación", LocalDate.of(1985, 3, 2), "F",
            "999111222", "prueba@example.com", "Calle Falsa 123");
        p.completarFiliacion("Trujillo", "Huamachuco", "CASADO", "SUPERIOR_TECNICA", "Docente", "Católica",
            "Pedro Filiación", "Esposo", "999333444");
        registro.registrarPacienteYAbrirHistoria(p, admision, "127.0.0.1");
        Paciente guardado = pacientes.findAll().stream()
            .filter(x -> "70000051".equals(x.getNroDocumento())).findFirst().orElseThrow();
        assertEquals("Trujillo", guardado.getLugarNacimiento());
        assertEquals("Huamachuco", guardado.getProcedencia());
        assertEquals("CASADO", guardado.getEstadoCivil());
        assertEquals("SUPERIOR_TECNICA", guardado.getGradoInstruccion());
        assertEquals("Docente", guardado.getOcupacion());
        assertEquals("Católica", guardado.getReligion());
        assertEquals("Pedro Filiación", guardado.getContactoEmergenciaNombre());
        assertEquals("Esposo", guardado.getContactoEmergenciaParentesco());
        assertEquals("999333444", guardado.getContactoEmergenciaTelefono());
    }
}
