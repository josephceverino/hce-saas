package pe.edu.ucv.hce.dominio;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Consentimiento de acceso del paciente a un tercero (tabla consentimiento_acceso). CU-11, RF-17; RN-06, RN-07, RN-09.
 * Se otorga y se revoca; al revocar se guarda la fecha y el acceso del tercero termina de inmediato.
 * Estados (DEC-13): ACTIVO y REVOCADO.
 */
@Entity
@Table(name = "consentimiento_acceso")
public class ConsentimientoAcceso {

    public static final String ACTIVO = "ACTIVO";
    public static final String REVOCADO = "REVOCADO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_consentimiento_acceso")
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;

    @Column(name = "otorgado_a", nullable = false, length = 150)
    private String otorgadoA;

    @Column(name = "alcance", nullable = false, length = 200)
    private String alcance;

    @Column(name = "fecha_otorgado", nullable = false)
    private LocalDateTime fechaOtorgado;

    @Column(name = "fecha_revocado")
    private LocalDateTime fechaRevocado;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    protected ConsentimientoAcceso() { }

    public ConsentimientoAcceso(Paciente paciente, String otorgadoA, String alcance, LocalDateTime fechaOtorgado) {
        this.paciente = paciente;
        this.otorgadoA = otorgadoA;
        this.alcance = alcance;
        this.fechaOtorgado = fechaOtorgado;
        this.estado = ACTIVO;
    }

    public boolean estaActivo() { return ACTIVO.equals(estado); }

    /** Revoca el consentimiento. Revocar uno ya revocado es un error (no se pisa la fecha original). */
    public void revocar(LocalDateTime fecha) {
        if (!estaActivo()) {
            throw new IllegalArgumentException("Este consentimiento ya fue revocado.");
        }
        this.estado = REVOCADO;
        this.fechaRevocado = fecha;
    }

    public Integer getId() { return id; }
    public Paciente getPaciente() { return paciente; }
    public String getOtorgadoA() { return otorgadoA; }
    public String getAlcance() { return alcance; }
    public LocalDateTime getFechaOtorgado() { return fechaOtorgado; }
    public LocalDateTime getFechaRevocado() { return fechaRevocado; }
    public String getEstado() { return estado; }
}
