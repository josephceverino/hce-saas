package pe.edu.ucv.hce.dominio;

import jakarta.persistence.*;
import java.time.LocalDate;

/** Suscripcion de un hospital a la plataforma (tabla suscripcion). CU-16, RF-20. */
@Entity
@Table(name = "suscripcion")
public class Suscripcion {

    public static final String ACTIVA = "ACTIVA";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_suscripcion")
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_establecimiento", nullable = false)
    private Establecimiento establecimiento;

    @Column(name = "plan", nullable = false, length = 50)
    private String plan;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    protected Suscripcion() { }

    public Suscripcion(Establecimiento establecimiento, String plan, LocalDate fechaInicio, LocalDate fechaFin) {
        this.establecimiento = establecimiento;
        this.plan = plan;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = ACTIVA;
    }

    public Integer getId() { return id; }
    public Establecimiento getEstablecimiento() { return establecimiento; }
    public String getPlan() { return plan; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public String getEstado() { return estado; }
}
