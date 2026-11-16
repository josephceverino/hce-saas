package pe.edu.ucv.hce.persistencia;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ucv.hce.dominio.Alergia;
import pe.edu.ucv.hce.dominio.Antecedente;
import pe.edu.ucv.hce.dominio.Atencion;
import pe.edu.ucv.hce.dominio.Diagnostico;
import pe.edu.ucv.hce.dominio.Establecimiento;
import pe.edu.ucv.hce.dominio.HistoriaClinica;
import pe.edu.ucv.hce.dominio.Paciente;
import pe.edu.ucv.hce.dominio.CodigoRol;
import pe.edu.ucv.hce.dominio.ProfesionalSalud;
import pe.edu.ucv.hce.dominio.Tratamiento;

/**
 * Datos clinicos FICTICIOS para que los tableros y pantallas de la demo tengan contenido (CU-05, CU-06;
 * docs/diseno_ui.md: las cifras salen de la base de datos). Todos los pacientes se llaman "Paciente de prueba".
 * Es idempotente: un paciente ya existente (por DNI) no se vuelve a crear. Se desactiva con hce.demo-datos=false
 * (asi estan los tests). Se cargan directo por repositorios, solo como semilla de demostracion.
 */
@Component
@Order(2)
@ConditionalOnProperty(name = "hce.demo-datos", havingValue = "true")
public class DatosDemoClinicos implements CommandLineRunner {

    private final EstablecimientoRepository establecimientos;
    private final UsuarioRepository usuarios;
    private final ProfesionalSaludRepository profesionales;
    private final PacienteRepository pacientes;
    private final HistoriaClinicaRepository historias;
    private final AtencionRepository atenciones;
    private final DiagnosticoRepository diagnosticos;
    private final TratamientoRepository tratamientos;
    private final AlergiaRepository alergias;
    private final AntecedenteRepository antecedentes;
    private final RolRepository roles;

    public DatosDemoClinicos(EstablecimientoRepository establecimientos, UsuarioRepository usuarios,
                             ProfesionalSaludRepository profesionales, PacienteRepository pacientes,
                             HistoriaClinicaRepository historias, AtencionRepository atenciones,
                             DiagnosticoRepository diagnosticos, TratamientoRepository tratamientos,
                             AlergiaRepository alergias, AntecedenteRepository antecedentes,
                             RolRepository roles) {
        this.establecimientos = establecimientos;
        this.usuarios = usuarios;
        this.profesionales = profesionales;
        this.pacientes = pacientes;
        this.historias = historias;
        this.atenciones = atenciones;
        this.diagnosticos = diagnosticos;
        this.tratamientos = tratamientos;
        this.alergias = alergias;
        this.antecedentes = antecedentes;
        this.roles = roles;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Establecimiento a = establecimientos.findByCodigoIpress("0000001").orElse(null);
        Establecimiento b = establecimientos.findByCodigoIpress("0000002").orElse(null);
        ProfesionalSalud medicoA = profesionalDe("medico.a");
        ProfesionalSalud medicoB = profesionalDe("medico.b");
        if (a == null || b == null || medicoA == null || medicoB == null) {
            return;
        }

        // Hospital A: 4 pacientes, 8 atenciones (6 cerradas, 2 abiertas), con alergias y antecedentes.
        HistoriaClinica h1 = paciente(a, "90000001", "Paciente de prueba", "Uno", LocalDate.of(1988, 4, 12), "F", 30);
        if (h1 != null) {
            alergias.save(new Alergia(h1, "Penicilina", "Urticaria", "ALTA"));
            antecedentes.save(new Antecedente(h1, "Personal", "Asma leve en la infancia", LocalDate.now().minusDays(30)));
            atencion(h1, medicoA, a, 9, "Dolor de garganta", "J02.9", "Faringitis aguda, no especificada", true);
            atencion(h1, medicoA, a, 2, "Tos y malestar general", "J06.9",
                "Infección aguda de las vías respiratorias superiores, no especificada", true);
        }
        HistoriaClinica h2 = paciente(a, "90000002", "Paciente de prueba", "Dos", LocalDate.of(1965, 11, 3), "M", 21);
        if (h2 != null) {
            antecedentes.save(new Antecedente(h2, "Personal", "Hipertensión en control", LocalDate.now().minusDays(60)));
            atencion(h2, medicoA, a, 7, "Control de presión arterial", "I10", "Hipertensión esencial (primaria)", true);
            atencion(h2, medicoA, a, 0, "Dolor de cabeza intenso", "R51", "Cefalea", false);
        }
        HistoriaClinica h3 = paciente(a, "90000003", "Paciente de prueba", "Tres", LocalDate.of(2001, 8, 25), "F", 12);
        if (h3 != null) {
            alergias.save(new Alergia(h3, "Polvo", "Rinitis", "LEVE"));
            atencion(h3, medicoA, a, 5, "Diarrea desde ayer", "A09",
                "Diarrea y gastroenteritis de presunto origen infeccioso", true);
            atencion(h3, medicoA, a, 1, "Fiebre persistente", "R50.9", "Fiebre, no especificada", true);
        }
        HistoriaClinica h4 = paciente(a, "90000004", "Paciente de prueba", "Cuatro", LocalDate.of(1978, 2, 14), "M", 5);
        if (h4 != null) {
            atencion(h4, medicoA, a, 3, "Dolor lumbar", "M54.5", "Lumbago no especificado", true);
            atencion(h4, medicoA, a, 0, "Ardor al orinar", "N39.0", "Infección de vías urinarias, sitio no especificado",
                false);
        }

        // Hospital B: 2 pacientes con una atencion cerrada cada uno (para probar el aislamiento entre hospitales).
        HistoriaClinica h5 = paciente(b, "90000005", "Paciente de prueba", "Cinco", LocalDate.of(1992, 6, 30), "M", 20);
        if (h5 != null) {
            atencion(h5, medicoB, b, 4, "Dolor de estómago", "K29.7", "Gastritis, no especificada", true);
        }
        HistoriaClinica h6 = paciente(b, "90000006", "Paciente de prueba", "Seis", LocalDate.of(1970, 9, 9), "F", 3);
        if (h6 != null) {
            atencion(h6, medicoB, b, 1, "Falta de aire", "J45.9", "Asma, no especificada", true);
        }

        // Portal del paciente (CU-10): cuentas sin hospital (el paciente es global, DEC-02), ligadas a un paciente.
        cuentaPaciente("paciente.a", "90000001");
        cuentaPaciente("paciente.b", "90000005");
    }

    /** Crea la cuenta del portal y la liga al paciente por DNI, si aun no existe. Clave demo123. */
    private void cuentaPaciente(String nombre, String dni) {
        var rol = roles.findByNombre(CodigoRol.PACIENTE.name()).orElse(null);
        if (rol == null || usuarios.findByUsuario(nombre).isPresent()) {
            return;
        }
        var paciente = pacientes.findByTipoDocumentoAndNroDocumento("DNI", dni).orElse(null);
        if (paciente == null || paciente.getUsuario() != null) {
            return;
        }
        paciente.vincularUsuario(usuarios.save(new pe.edu.ucv.hce.dominio.Usuario(null, rol, nombre,
            pe.edu.ucv.hce.aplicacion.ClaveUtil.bcrypt("demo123"), "ACTIVO")));
        pacientes.save(paciente);
    }

    private ProfesionalSalud profesionalDe(String usuario) {
        return usuarios.findByUsuario(usuario)
            .flatMap(u -> profesionales.findByUsuarioId(u.getId())).orElse(null);
    }

    /** Crea paciente e historia si el DNI no existe; si ya existe devuelve null (no se vuelve a sembrar). */
    private HistoriaClinica paciente(Establecimiento hospital, String dni, String nombres, String apellidos,
                                     LocalDate nacimiento, String sexo, int diasDesdeApertura) {
        if (pacientes.existsByTipoDocumentoAndNroDocumento("DNI", dni)) {
            return null;
        }
        Paciente p = pacientes.save(new Paciente("DNI", dni, nombres, apellidos, nacimiento, sexo,
            "999000000", "demo@example.com", "Dirección ficticia 123"));
        HistoriaClinica h = historias.save(new HistoriaClinica(p, hospital, "TMP-" + dni, LocalDate.now().minusDays(diasDesdeApertura),
            "ABIERTA"));
        h.setNroHc(String.format("HC-%06d", h.getId()));
        return h;
    }

    private void atencion(HistoriaClinica historia, ProfesionalSalud medico, Establecimiento hospital, int diasAtras,
                          String motivo, String codigo, String descripcion, boolean cerrada) {
        LocalDateTime fecha = LocalDateTime.now().minusDays(diasAtras).withHour(9 + diasAtras % 6).withMinute(15);
        Atencion at = atenciones.save(new Atencion(historia, medico, hospital, fecha, motivo,
            "Cuadro de " + (diasAtras + 1) + " día(s) de evolución (dato ficticio).", "Examen sin hallazgos graves (dato ficticio)."));
        diagnosticos.save(new Diagnostico(at, codigo, descripcion, Diagnostico.PRINCIPAL));
        if (cerrada) {
            tratamientos.save(new Tratamiento(at, "Reposo e hidratación", "Paracetamol", "500 mg cada 8 horas", "3 días"));
            at.cerrar();
            atenciones.save(at);
        }
    }
}
