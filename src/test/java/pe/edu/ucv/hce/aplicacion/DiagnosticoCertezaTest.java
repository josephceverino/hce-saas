package pe.edu.ucv.hce.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ucv.hce.dominio.Diagnostico;
import pe.edu.ucv.hce.dominio.Paciente;
import pe.edu.ucv.hce.dominio.Usuario;
import pe.edu.ucv.hce.persistencia.UsuarioRepository;

/** D-43: el diagnostico es presuntivo o definitivo (docs/historia_clinica_contenido.md, seccion 4; CU-05; RN-10). */
@SpringBootTest
@Transactional
class DiagnosticoCertezaTest {

    private static final String IP = "127.0.0.1";

    @Autowired AtencionService atenciones;
    @Autowired RegistroPacienteService registro;
    @Autowired UsuarioRepository usuarios;

    Usuario medicoA;
    Integer idAtencion;

    @BeforeEach
    void escenario() {
        Usuario admision = usuarios.findByUsuario("admision.a").orElseThrow();
        medicoA = usuarios.findByUsuario("medico.a").orElseThrow();
        var historia = registro.registrarPacienteYAbrirHistoria(new Paciente("DNI", "70000141", "Paciente de prueba",
            "Certeza", LocalDate.of(1990, 1, 1), "F", "999111222", "prueba@example.com", "Calle Falsa 123"),
            admision, IP).historia();
        idAtencion = atenciones.crear(medicoA, historia.getId(), "Dolor", "A", "E", IP).atencion().getId();
    }

    @Test
    void guardaLaCertezaIndicada() {
        var d = atenciones.agregarDiagnostico(medicoA, idAtencion, "J02.9", Diagnostico.PRINCIPAL, Diagnostico.PRESUNTIVO, IP);
        assertEquals(Diagnostico.PRESUNTIVO, d.getCerteza());
    }

    @Test
    void sinCertezaSeTomaComoDefinitivo() {
        assertEquals(Diagnostico.DEFINITIVO,
            atenciones.agregarDiagnostico(medicoA, idAtencion, "J02.9", Diagnostico.PRINCIPAL, null, IP).getCerteza());
        assertEquals(Diagnostico.DEFINITIVO,
            atenciones.agregarDiagnostico(medicoA, idAtencion, "I10", Diagnostico.SECUNDARIO, IP).getCerteza());
    }

    @Test
    void unaCertezaInventadaSeRechaza() {
        assertThrows(IllegalArgumentException.class,
            () -> atenciones.agregarDiagnostico(medicoA, idAtencion, "J02.9", Diagnostico.PRINCIPAL, "QUIZAS", IP));
    }
}
