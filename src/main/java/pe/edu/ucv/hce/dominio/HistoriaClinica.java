package pe.edu.ucv.hce.dominio;

import jakarta.persistence.*;
import java.time.LocalDate;

/** Historia clinica: una por paciente (RN-01), ligada al establecimiento que la abrio. */
@Entity
@Table(name = "historia_clinica",
       uniqueConstraints = @UniqueConstraint(name = "uq_historia_nro", columnNames = "nro_hc"))
public class HistoriaClinica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historia_clinica")
    private Integer id;

    @OneToOne(optional = false)
    @JoinColumn(name = "id_paciente", nullable = false, unique = true)
    private Paciente paciente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_establecimiento", nullable = false)
    private Establecimiento establecimiento;

    @Column(name = "nro_hc", nullable = false, length = 20)
    private String nroHc;

    @Column(name = "fecha_apertura", nullable = false)
    private LocalDate fechaApertura;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    protected HistoriaClinica() { }

    public HistoriaClinica(Paciente paciente, Establecimiento establecimiento, String nroHc,
                           LocalDate fechaApertura, String estado) {
        this.paciente = paciente;
        this.establecimiento = establecimiento;
        this.nroHc = nroHc;
        this.fechaApertura = fechaApertura;
        this.estado = estado;
    }

    public Integer getId() { return id; }
    public Paciente getPaciente() { return paciente; }
    public Establecimiento getEstablecimiento() { return establecimiento; }
    public String getNroHc() { return nroHc; }
    public void setNroHc(String nroHc) { this.nroHc = nroHc; }
    public LocalDate getFechaApertura() { return fechaApertura; }
    public String getEstado() { return estado; }
}
