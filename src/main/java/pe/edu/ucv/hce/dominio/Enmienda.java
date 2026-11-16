package pe.edu.ucv.hce.dominio;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Enmienda: correccion de una atencion cerrada, con autor y fecha (tabla enmienda). RN-03, DD-05.
 * Texto libre: motivo y contenido corregido. No tiene setters.
 */
@Entity
@Table(name = "enmienda")
public class Enmienda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_enmienda")
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_atencion", nullable = false)
    private Atencion atencion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_profesional_salud", nullable = false)
    private ProfesionalSalud profesional;

    @Column(name = "motivo", nullable = false, length = 200)
    private String motivo;

    @Column(name = "contenido_corregido", nullable = false, columnDefinition = "TEXT")
    private String contenidoCorregido;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    protected Enmienda() { }

    public Enmienda(Atencion atencion, ProfesionalSalud profesional, String motivo, String contenidoCorregido,
                    LocalDateTime fecha) {
        this.atencion = atencion;
        this.profesional = profesional;
        this.motivo = motivo;
        this.contenidoCorregido = contenidoCorregido;
        this.fecha = fecha;
    }

    public Integer getId() { return id; }
    public Atencion getAtencion() { return atencion; }
    public ProfesionalSalud getProfesional() { return profesional; }
    public String getMotivo() { return motivo; }
    public String getContenidoCorregido() { return contenidoCorregido; }
    public LocalDateTime getFecha() { return fecha; }
}
