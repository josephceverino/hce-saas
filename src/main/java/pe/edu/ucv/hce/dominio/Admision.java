package pe.edu.ucv.hce.dominio;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Admision del paciente del dia (tabla admision). CU-20, RF-21, RF-22; RN-13, RN-06.
 * No es una cita: registra a quien ya esta presente en el hospital. Su estado solo avanza en el orden de RN-13.
 */
@Entity
@Table(name = "admision")
public class Admision {

    public static final String CONSULTA_EXTERNA = "CONSULTA_EXTERNA";
    public static final String EMERGENCIA = "EMERGENCIA";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_admision")
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_historia_clinica", nullable = false)
    private HistoriaClinica historiaClinica;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_establecimiento", nullable = false)
    private Establecimiento establecimiento;

    @Column(name = "tipo_atencion", nullable = false, length = 20)
    private String tipoAtencion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 25)
    private EstadoAdmision estado;

    @Column(name = "fecha_hora_admision", nullable = false)
    private LocalDateTime fechaHoraAdmision;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_usuario_admite", nullable = false)
    private Usuario usuarioAdmite;

    /** Ultimo cambio de estado: sirve para ordenar la cola y mostrar cuanto lleva esperando. */
    @Column(name = "fecha_hora_estado", nullable = false)
    private LocalDateTime fechaHoraEstado;

    protected Admision() { }

    public Admision(HistoriaClinica historiaClinica, String tipoAtencion, Usuario usuarioAdmite, LocalDateTime ahora) {
        if (!CONSULTA_EXTERNA.equals(tipoAtencion) && !EMERGENCIA.equals(tipoAtencion)) {
            throw new IllegalArgumentException("El tipo de atención debe ser consulta externa o emergencia.");
        }
        this.historiaClinica = historiaClinica;
        this.establecimiento = historiaClinica.getEstablecimiento();
        this.tipoAtencion = tipoAtencion;
        this.usuarioAdmite = usuarioAdmite;
        this.fechaHoraAdmision = ahora;
        this.fechaHoraEstado = ahora;
        this.estado = EstadoAdmision.ADMITIDO;
    }

    /** RN-13: cambia de estado solo si el orden lo permite; si no, lanza TransicionInvalidaException. */
    public void pasarA(EstadoAdmision destino, LocalDateTime ahora) {
        if (!estado.puedePasarA(destino)) {
            throw new TransicionInvalidaException("La admisión está " + estado.descripcion().toLowerCase()
                + " y no puede pasar a " + destino.descripcion().toLowerCase() + ".");
        }
        this.estado = destino;
        this.fechaHoraEstado = ahora;
    }

    public boolean esEmergencia() { return EMERGENCIA.equals(tipoAtencion); }

    public Integer getId() { return id; }
    public HistoriaClinica getHistoriaClinica() { return historiaClinica; }
    public Establecimiento getEstablecimiento() { return establecimiento; }
    public String getTipoAtencion() { return tipoAtencion; }
    public EstadoAdmision getEstado() { return estado; }
    public LocalDateTime getFechaHoraAdmision() { return fechaHoraAdmision; }
    public Usuario getUsuarioAdmite() { return usuarioAdmite; }
    public LocalDateTime getFechaHoraEstado() { return fechaHoraEstado; }
}
