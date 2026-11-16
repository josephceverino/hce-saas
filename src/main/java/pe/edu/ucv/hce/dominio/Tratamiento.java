package pe.edu.ucv.hce.dominio;

import jakarta.persistence.*;

/** Tratamiento e indicaciones de una atencion (tabla tratamiento). CU-08, RF-08; RN-02, RN-03, RN-11. */
@Entity
@Table(name = "tratamiento")
public class Tratamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tratamiento")
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_atencion", nullable = false)
    private Atencion atencion;

    @Column(name = "indicacion", nullable = false, columnDefinition = "TEXT")
    private String indicacion;

    @Column(name = "medicamento", nullable = false, length = 150)
    private String medicamento;

    @Column(name = "dosis", nullable = false, length = 100)
    private String dosis;

    @Column(name = "duracion", nullable = false, length = 100)
    private String duracion;

    protected Tratamiento() { }

    public Tratamiento(Atencion atencion, String indicacion, String medicamento, String dosis, String duracion) {
        this.atencion = atencion;
        this.indicacion = indicacion;
        this.medicamento = medicamento;
        this.dosis = dosis;
        this.duracion = duracion;
    }

    public Integer getId() { return id; }
    public Atencion getAtencion() { return atencion; }
    public String getIndicacion() { return indicacion; }
    public String getMedicamento() { return medicamento; }
    public String getDosis() { return dosis; }
    public String getDuracion() { return duracion; }
}
