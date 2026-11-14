package pe.edu.ucv.hce.dominio;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Usuario de un hospital (tabla usuario). Pertenece a un establecimiento: base del SaaS multi-hospital. */
@Entity
@Table(name = "usuario",
       uniqueConstraints = @UniqueConstraint(name = "uq_usuario_nombre", columnNames = "usuario"))
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_establecimiento")
    private Establecimiento establecimiento;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    @Column(name = "usuario", nullable = false, length = 50)
    private String usuario;

    @Column(name = "contrasena_hash", nullable = false, length = 255)
    private String contrasenaHash;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    protected Usuario() { }

    public Usuario(Establecimiento establecimiento, Rol rol, String usuario,
                   String contrasenaHash, String estado) {
        this.establecimiento = establecimiento;
        this.rol = rol;
        this.usuario = usuario;
        this.contrasenaHash = contrasenaHash;
        this.estado = estado;
    }

    public Integer getId() { return id; }
    public Establecimiento getEstablecimiento() { return establecimiento; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    /** DEC-03: true si el codigo de rol (rol.nombre) es el indicado. */
    public boolean tieneRol(CodigoRol codigo) {
        return rol != null && codigo.name().equals(rol.getNombre());
    }
    public String getUsuario() { return usuario; }
    public String getContrasenaHash() { return contrasenaHash; }
    public String getEstado() { return estado; }
    public LocalDateTime getUltimoAcceso() { return ultimoAcceso; }
    public void setUltimoAcceso(LocalDateTime ultimoAcceso) { this.ultimoAcceso = ultimoAcceso; }
}
