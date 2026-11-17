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

    /* Plan v3, 1.3 (RF-01, RF-03): filiacion completa. Nulos en pacientes registrados antes de v3. */
    @Column(name = "lugar_nacimiento", length = 100)
    private String lugarNacimiento;

    @Column(name = "procedencia", length = 100)
    private String procedencia;

    @Column(name = "estado_civil", length = 30)
    private String estadoCivil;

    @Column(name = "grado_instruccion", length = 40)
    private String gradoInstruccion;

    @Column(name = "ocupacion", length = 100)
    private String ocupacion;

    @Column(name = "religion", length = 60)
    private String religion;

    @Column(name = "contacto_emergencia_nombre", length = 150)
    private String contactoEmergenciaNombre;

    @Column(name = "contacto_emergencia_parentesco", length = 40)
    private String contactoEmergenciaParentesco;

    @Column(name = "contacto_emergencia_telefono", length = 20)
    private String contactoEmergenciaTelefono;

    /** Usuario del portal del paciente (CU-10). Nulo si el paciente no tiene cuenta. Un paciente es global (DEC-02). */
    @OneToOne
    @JoinColumn(name = "id_usuario", unique = true)
    private Usuario usuario;

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

    public Usuario getUsuario() { return usuario; }
    public String getLugarNacimiento() { return lugarNacimiento; }
    public String getProcedencia() { return procedencia; }
    public String getEstadoCivil() { return estadoCivil; }
    public String getGradoInstruccion() { return gradoInstruccion; }
    public String getOcupacion() { return ocupacion; }
    public String getReligion() { return religion; }
    public String getContactoEmergenciaNombre() { return contactoEmergenciaNombre; }
    public String getContactoEmergenciaParentesco() { return contactoEmergenciaParentesco; }
    public String getContactoEmergenciaTelefono() { return contactoEmergenciaTelefono; }

    /** RF-01: completa la filiacion (lugar de nacimiento, datos sociales y contacto de emergencia). */
    public void completarFiliacion(String lugarNacimiento, String procedencia, String estadoCivil,
                                   String gradoInstruccion, String ocupacion, String religion,
                                   String contactoEmergenciaNombre, String contactoEmergenciaParentesco,
                                   String contactoEmergenciaTelefono) {
        this.lugarNacimiento = lugarNacimiento;
        this.procedencia = procedencia;
        this.estadoCivil = estadoCivil;
        this.gradoInstruccion = gradoInstruccion;
        this.ocupacion = ocupacion;
        this.religion = religion;
        this.contactoEmergenciaNombre = contactoEmergenciaNombre;
        this.contactoEmergenciaParentesco = contactoEmergenciaParentesco;
        this.contactoEmergenciaTelefono = contactoEmergenciaTelefono;
    }

    /** Vincula la cuenta del portal (RN-04: el paciente entra solo a lo suyo a traves de este vinculo). */
    public void vincularUsuario(Usuario usuario) { this.usuario = usuario; }
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
