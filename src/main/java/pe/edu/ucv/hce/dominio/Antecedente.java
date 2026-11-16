package pe.edu.ucv.hce.dominio;

import jakarta.persistence.*;
import java.time.LocalDate;

/** Antecedente del paciente (tabla antecedente). RF-07; se muestra en CU-06. En la demo, datos ficticios. */
@Entity
@Table(name = "antecedente")
public class Antecedente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_antecedente")
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_historia_clinica", nullable = false)
    private HistoriaClinica historiaClinica;

    @Column(name = "tipo", nullable = false, length = 50)
    private String tipo;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDate fechaRegistro;

    protected Antecedente() { }

    public Antecedente(HistoriaClinica historiaClinica, String tipo, String descripcion, LocalDate fechaRegistro) {
        this.historiaClinica = historiaClinica;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.fechaRegistro = fechaRegistro;
    }

    public Integer getId() { return id; }
    public HistoriaClinica getHistoriaClinica() { return historiaClinica; }
    public String getTipo() { return tipo; }
    public String getDescripcion() { return descripcion; }
    public LocalDate getFechaRegistro() { return fechaRegistro; }
}
