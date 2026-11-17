package pe.edu.ucv.hce.dominio;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Solicitud de examen auxiliar hecha en una atencion (tabla solicitud_examen). CU-22, RF-25; RN-02, RN-18, RN-06.
 * SOLICITADO hasta que apoyo al diagnostico registra el resultado (CU-23); entonces pasa a RESULTADO_REGISTRADO.
 */
@Entity
@Table(name = "solicitud_examen")
public class SolicitudExamen {

    public static final String SOLICITADO = "SOLICITADO";
    public static final String RESULTADO_REGISTRADO = "RESULTADO_REGISTRADO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud_examen")
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_atencion", nullable = false)
    private Atencion atencion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_establecimiento", nullable = false)
    private Establecimiento establecimiento;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_profesional_salud", nullable = false)
    private ProfesionalSalud solicitante;

    @Column(name = "tipo_examen", nullable = false, length = 100)
    private String tipoExamen;

    @Column(name = "indicaciones", columnDefinition = "TEXT")
    private String indicaciones;

    @Column(name = "estado", nullable = false, length = 25)
    private String estado;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    /** Resultado, cuando ya se registro (lado inverso de resultado_examen.id_solicitud_examen). */
    @OneToOne(mappedBy = "solicitud", fetch = FetchType.EAGER)
    private ResultadoExamen resultado;

    protected SolicitudExamen() { }

    public SolicitudExamen(Atencion atencion, ProfesionalSalud solicitante, String tipoExamen, String indicaciones,
                           LocalDateTime fechaHora) {
        this.atencion = atencion;
        this.establecimiento = atencion.getEstablecimiento();
        this.solicitante = solicitante;
        this.tipoExamen = tipoExamen;
        this.indicaciones = indicaciones;
        this.fechaHora = fechaHora;
        this.estado = SOLICITADO;
    }

    /** RN-18: el resultado solo entra sobre una solicitud en estado SOLICITADO. */
    public void registrarResultado(ResultadoExamen resultado) {
        if (!SOLICITADO.equals(estado)) {
            throw new IllegalArgumentException("Esa solicitud ya tiene un resultado registrado.");
        }
        this.resultado = resultado;
        this.estado = RESULTADO_REGISTRADO;
    }

    public Integer getId() { return id; }
    public Atencion getAtencion() { return atencion; }
    public Establecimiento getEstablecimiento() { return establecimiento; }
    public ProfesionalSalud getSolicitante() { return solicitante; }
    public String getTipoExamen() { return tipoExamen; }
    public String getIndicaciones() { return indicaciones; }
    public String getEstado() { return estado; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public ResultadoExamen getResultado() { return resultado; }
}
