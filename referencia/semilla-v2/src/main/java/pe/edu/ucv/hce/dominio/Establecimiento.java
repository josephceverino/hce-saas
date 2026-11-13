package pe.edu.ucv.hce.dominio;

import jakarta.persistence.*;

/** Hospital o establecimiento de salud que usa la plataforma (tabla establecimiento). */
@Entity
@Table(name = "establecimiento")
public class Establecimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_establecimiento")
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "codigo_ipress", nullable = false, length = 20)
    private String codigoIpress;

    @Column(name = "region", nullable = false, length = 50)
    private String region;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    protected Establecimiento() { }

    public Establecimiento(String nombre, String codigoIpress, String region, String estado) {
        this.nombre = nombre;
        this.codigoIpress = codigoIpress;
        this.region = region;
        this.estado = estado;
    }

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCodigoIpress() { return codigoIpress; }
    public String getRegion() { return region; }
    public String getEstado() { return estado; }
}
