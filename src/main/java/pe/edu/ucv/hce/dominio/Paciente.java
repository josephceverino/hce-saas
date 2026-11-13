package pe.edu.ucv.hce.dominio;

import jakarta.persistence.*;
import java.time.LocalDate;

/** Persona atendida (tabla paciente). El documento no se repite (RN-01). */
@Entity
@Table(name = "paciente",
       uniqueConstraints = @UniqueConstraint(name = "uq_paciente_documento",
                                             columnNames = {"tipo_documento", "nro_documento"}))
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paciente")
    private Integer id;

    @Column(name = "tipo_documento", nullable = false, length = 20)
    private String tipoDocumento;

    @Column(name = "nro_documento", nullable = false, length = 20)
    private String nroDocumento;

    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @Column(name = "apellidos", nullable = false, length = 100)
    private String apellidos;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(name = "sexo", nullable = false, length = 10)
    private String sexo;

    @Column(name = "telefono", nullable = false, length = 20)
    private String telefono;

    @Column(name = "correo", nullable = false, length = 100)
    private String correo;

    @Column(name = "direccion", nullable = false, length = 200)
    private String direccion;

    protected Paciente() { }

    public Paciente(String tipoDocumento, String nroDocumento, String nombres, String apellidos,
                    LocalDate fechaNacimiento, String sexo, String telefono, String correo,
                    String direccion) {
        this.tipoDocumento = tipoDocumento;
        this.nroDocumento = nroDocumento;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.fechaNacimiento = fechaNacimiento;
        this.sexo = sexo;
        this.telefono = telefono;
        this.correo = correo;
        this.direccion = direccion;
    }

    public Integer getId() { return id; }
    public String getTipoDocumento() { return tipoDocumento; }
    public String getNroDocumento() { return nroDocumento; }
    public String getNombres() { return nombres; }
    public String getApellidos() { return apellidos; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public String getSexo() { return sexo; }
    public String getTelefono() { return telefono; }
    public String getCorreo() { return correo; }
    public String getDireccion() { return direccion; }
}
