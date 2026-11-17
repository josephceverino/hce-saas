package pe.edu.ucv.hce.dominio;

import jakarta.persistence.*;
import java.time.LocalDate;

/** Resultado de un examen (tabla resultado_examen). CU-23, RF-09; RN-18, RN-06. Sin archivo adjunto en la demo (D-41). */
@Entity
@Table(name = "resultado_examen")
public class ResultadoExamen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_resultado_examen")
    private Integer id;

    @OneToOne(optional = false)
    @JoinColumn(name = "id_solicitud_examen", nullable = false, unique = true)
    private SolicitudExamen solicitud;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_atencion", nullable = false)
    private Atencion atencion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_profesional_salud", nullable = false)
    private ProfesionalSalud profesional;

    @Column(name = "tipo_examen", nullable = false, length = 100)
    private String tipoExamen;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "resultado", nullable = false, columnDefinition = "TEXT")
    private String resultado;

    @Column(name = "archivo_adjunto", length = 255)
    private String archivoAdjunto;

    protected ResultadoExamen() { }

    public ResultadoExamen(SolicitudExamen solicitud, ProfesionalSalud profesional, LocalDate fecha, String resultado) {
        this.solicitud = solicitud;
        this.atencion = solicitud.getAtencion();
        this.profesional = profesional;
        this.tipoExamen = solicitud.getTipoExamen();
        this.fecha = fecha;
        this.resultado = resultado;
    }

    public Integer getId() { return id; }
    public SolicitudExamen getSolicitud() { return solicitud; }
    public Atencion getAtencion() { return atencion; }
    public ProfesionalSalud getProfesional() { return profesional; }
    public String getTipoExamen() { return tipoExamen; }
    public LocalDate getFecha() { return fecha; }
    public String getResultado() { return resultado; }
}
