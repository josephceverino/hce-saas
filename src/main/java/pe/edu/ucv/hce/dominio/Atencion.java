package pe.edu.ucv.hce.dominio;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Atencion medica (tabla atencion). CU-05, RF-05; RN-02, RN-03, RN-11.
 * RN-11: pertenece a una sola historia (paciente), un solo profesional y un solo establecimiento.
 * RN-03: una vez CERRADA no se modifica ni se elimina; las correcciones van como Enmienda.
 */
@Entity
@Table(name = "atencion")
public class Atencion {

    public static final String ABIERTA = "ABIERTA";
    public static final String CERRADA = "CERRADA";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_atencion")
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_historia_clinica", nullable = false)
    private HistoriaClinica historiaClinica;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_profesional_salud", nullable = false)
    private ProfesionalSalud profesional;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_establecimiento", nullable = false)
    private Establecimiento establecimiento;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "motivo", nullable = false, columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "anamnesis", nullable = false, columnDefinition = "TEXT")
    private String anamnesis;

    @Column(name = "examen_fisico", nullable = false, columnDefinition = "TEXT")
    private String examenFisico;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    protected Atencion() { }

    public Atencion(HistoriaClinica historiaClinica, ProfesionalSalud profesional, Establecimiento establecimiento,
                    LocalDateTime fechaHora, String motivo, String anamnesis, String examenFisico) {
        this.historiaClinica = historiaClinica;
        this.profesional = profesional;
        this.establecimiento = establecimiento;
        this.fechaHora = fechaHora;
        this.motivo = motivo;
        this.anamnesis = anamnesis;
        this.examenFisico = examenFisico;
        this.estado = ABIERTA;
    }

    public boolean estaCerrada() { return CERRADA.equals(estado); }

    public Integer getId() { return id; }
    public HistoriaClinica getHistoriaClinica() { return historiaClinica; }
    public ProfesionalSalud getProfesional() { return profesional; }
    public Establecimiento getEstablecimiento() { return establecimiento; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public String getMotivo() { return motivo; }
    public String getAnamnesis() { return anamnesis; }
    public String getExamenFisico() { return examenFisico; }
    public String getEstado() { return estado; }
    /** RN-03: cierra la atencion. Una atencion ya cerrada no vuelve a cerrarse. */
    public void cerrar() {
        if (estaCerrada()) {
            throw new AtencionCerradaException("La atención ya está cerrada.");
        }
        this.estado = CERRADA;
    }
    // Sin setters de contenido (RN-03): lo cerrado se corrige solo con Enmienda.
}
