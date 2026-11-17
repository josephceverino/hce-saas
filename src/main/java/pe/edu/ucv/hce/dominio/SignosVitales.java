package pe.edu.ucv.hce.dominio;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Signos vitales de la admision del dia (tabla signos_vitales). CU-21, RF-23; RN-17.
 * Los registra enfermeria y se rechazan los valores fuera de los rangos de validacion.
 * RANGOS DE DEMOSTRACION, NO CLINICOS: solo descartan valores imposibles o mal digitados. Los define el equipo.
 */
@Entity
@Table(name = "signos_vitales")
public class SignosVitales {

    /** Rangos de demostracion (no clinicos): {minimo, maximo} por signo. */
    public static final int[] SISTOLICA = {50, 260};
    public static final int[] DIASTOLICA = {30, 160};
    public static final int[] FRECUENCIA_CARDIACA = {20, 250};
    public static final int[] FRECUENCIA_RESPIRATORIA = {4, 60};
    public static final BigDecimal[] TEMPERATURA = {new BigDecimal("30.0"), new BigDecimal("44.0")};
    public static final int[] SATURACION = {50, 100};

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_signos_vitales")
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_admision", nullable = false)
    private Admision admision;

    @Column(name = "presion_sistolica", nullable = false)
    private Integer presionSistolica;

    @Column(name = "presion_diastolica", nullable = false)
    private Integer presionDiastolica;

    @Column(name = "frecuencia_cardiaca", nullable = false)
    private Integer frecuenciaCardiaca;

    @Column(name = "frecuencia_respiratoria", nullable = false)
    private Integer frecuenciaRespiratoria;

    @Column(name = "temperatura", nullable = false, precision = 4, scale = 1)
    private BigDecimal temperatura;

    @Column(name = "saturacion_oxigeno", nullable = false)
    private Integer saturacionOxigeno;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    protected SignosVitales() { }

    /** RN-17: valida cada valor contra su rango de demostracion antes de crear el registro. */
    public SignosVitales(Admision admision, Integer presionSistolica, Integer presionDiastolica,
                         Integer frecuenciaCardiaca, Integer frecuenciaRespiratoria, BigDecimal temperatura,
                         Integer saturacionOxigeno, Usuario usuario, LocalDateTime fechaHora) {
        validar("La presión arterial sistólica", presionSistolica, SISTOLICA, "milímetros de mercurio");
        validar("La presión arterial diastólica", presionDiastolica, DIASTOLICA, "milímetros de mercurio");
        if (presionDiastolica >= presionSistolica) {
            throw new IllegalArgumentException("La presión diastólica debe ser menor que la sistólica.");
        }
        validar("La frecuencia cardíaca", frecuenciaCardiaca, FRECUENCIA_CARDIACA, "latidos por minuto");
        validar("La frecuencia respiratoria", frecuenciaRespiratoria, FRECUENCIA_RESPIRATORIA, "respiraciones por minuto");
        if (temperatura == null || temperatura.compareTo(TEMPERATURA[0]) < 0 || temperatura.compareTo(TEMPERATURA[1]) > 0) {
            throw new IllegalArgumentException("La temperatura debe estar entre " + TEMPERATURA[0] + " y "
                + TEMPERATURA[1] + " grados Celsius.");
        }
        validar("La saturación de oxígeno", saturacionOxigeno, SATURACION, "por ciento");
        this.admision = admision;
        this.presionSistolica = presionSistolica;
        this.presionDiastolica = presionDiastolica;
        this.frecuenciaCardiaca = frecuenciaCardiaca;
        this.frecuenciaRespiratoria = frecuenciaRespiratoria;
        this.temperatura = temperatura;
        this.saturacionOxigeno = saturacionOxigeno;
        this.usuario = usuario;
        this.fechaHora = fechaHora;
    }

    private static void validar(String nombre, Integer valor, int[] rango, String unidad) {
        if (valor == null || valor < rango[0] || valor > rango[1]) {
            throw new IllegalArgumentException(nombre + " debe estar entre " + rango[0] + " y " + rango[1] + " " + unidad + ".");
        }
    }

    public Integer getId() { return id; }
    public Admision getAdmision() { return admision; }
    public Integer getPresionSistolica() { return presionSistolica; }
    public Integer getPresionDiastolica() { return presionDiastolica; }
    public Integer getFrecuenciaCardiaca() { return frecuenciaCardiaca; }
    public Integer getFrecuenciaRespiratoria() { return frecuenciaRespiratoria; }
    public BigDecimal getTemperatura() { return temperatura; }
    public Integer getSaturacionOxigeno() { return saturacionOxigeno; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public Usuario getUsuario() { return usuario; }
}
