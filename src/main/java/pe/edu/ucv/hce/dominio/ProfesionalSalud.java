package pe.edu.ucv.hce.dominio;

import jakarta.persistence.*;

/**
 * Profesional de salud ligado a un usuario (tabla profesional_salud).
 * Necesario para CU-05: una atencion pertenece a un solo profesional (RN-11).
 */
@Entity
@Table(name = "profesional_salud",
       uniqueConstraints = @UniqueConstraint(name = "uq_profesional_usuario", columnNames = "id_usuario"))
public class ProfesionalSalud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_profesional_salud")
    private Integer id;

    @OneToOne(optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @Column(name = "apellidos", nullable = false, length = 100)
    private String apellidos;

    @Column(name = "colegiatura", nullable = false, length = 20)
    private String colegiatura;

    @Column(name = "especialidad", nullable = false, length = 100)
    private String especialidad;

    @Column(name = "tipo", nullable = false, length = 20)
    private String tipo;

    protected ProfesionalSalud() { }

    public ProfesionalSalud(Usuario usuario, String nombres, String apellidos, String colegiatura,
                            String especialidad, String tipo) {
        this.usuario = usuario;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.colegiatura = colegiatura;
        this.especialidad = especialidad;
        this.tipo = tipo;
    }

    public Integer getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public String getNombres() { return nombres; }
    public String getApellidos() { return apellidos; }
    public String getColegiatura() { return colegiatura; }
    public String getEspecialidad() { return especialidad; }
    public String getTipo() { return tipo; }
}
