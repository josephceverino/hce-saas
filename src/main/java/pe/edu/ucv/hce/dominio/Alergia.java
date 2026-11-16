package pe.edu.ucv.hce.dominio;

import jakarta.persistence.*;

/** Alergia del paciente (tabla alergia). RF-07; se muestra en CU-06 como chips rojos. Datos ficticios en la demo. */
@Entity
@Table(name = "alergia")
public class Alergia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alergia")
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_historia_clinica", nullable = false)
    private HistoriaClinica historiaClinica;

    @Column(name = "agente", nullable = false, length = 100)
    private String agente;

    @Column(name = "reaccion", nullable = false, length = 200)
    private String reaccion;

    @Column(name = "gravedad", nullable = false, length = 20)
    private String gravedad;

    protected Alergia() { }

    public Alergia(HistoriaClinica historiaClinica, String agente, String reaccion, String gravedad) {
        this.historiaClinica = historiaClinica;
        this.agente = agente;
        this.reaccion = reaccion;
        this.gravedad = gravedad;
    }

    public Integer getId() { return id; }
    public HistoriaClinica getHistoriaClinica() { return historiaClinica; }
    public String getAgente() { return agente; }
    public String getReaccion() { return reaccion; }
    public String getGravedad() { return gravedad; }
}
