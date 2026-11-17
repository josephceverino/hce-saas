package pe.edu.ucv.hce.dominio;

import jakarta.persistence.*;

/** Diagnostico de una atencion con codigo CIE-10 (tabla diagnostico). CU-05; RN-10. */
@Entity
@Table(name = "diagnostico")
public class Diagnostico {

    public static final String PRINCIPAL = "PRINCIPAL";
    public static final String SECUNDARIO = "SECUNDARIO";
    /** Certeza del diagnostico (docs/historia_clinica_contenido.md, seccion 4; D-43). */
    public static final String PRESUNTIVO = "PRESUNTIVO";
    public static final String DEFINITIVO = "DEFINITIVO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_diagnostico")
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_atencion", nullable = false)
    private Atencion atencion;

    @Column(name = "codigo_cie10", nullable = false, length = 10)
    private String codigoCie10;

    @Column(name = "descripcion", nullable = false, length = 200)
    private String descripcion;

    @Column(name = "tipo", nullable = false, length = 20)
    private String tipo;

    /** Presuntivo o definitivo. Los diagnosticos anteriores a este campo quedan como definitivos. */
    @Column(name = "certeza", nullable = false, length = 15,
            columnDefinition = "varchar(15) default 'DEFINITIVO' not null")
    private String certeza = DEFINITIVO;

    protected Diagnostico() { }

    public Diagnostico(Atencion atencion, String codigoCie10, String descripcion, String tipo) {
        this.atencion = atencion;
        this.codigoCie10 = codigoCie10;
        this.descripcion = descripcion;
        this.tipo = tipo;
    }

    public Diagnostico(Atencion atencion, String codigoCie10, String descripcion, String tipo, String certeza) {
        this(atencion, codigoCie10, descripcion, tipo);
        this.certeza = certeza;
    }

    public Integer getId() { return id; }
    public Atencion getAtencion() { return atencion; }
    public String getCodigoCie10() { return codigoCie10; }
    public String getDescripcion() { return descripcion; }
    public String getTipo() { return tipo; }
    public String getCerteza() { return certeza; }
}
