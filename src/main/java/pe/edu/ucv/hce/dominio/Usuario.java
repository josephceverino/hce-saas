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

    /** RN-14: la cuenta la creo otra persona con clave temporal; debe cambiarla en el primer ingreso (CU-18). */
    @Column(name = "debe_cambiar_clave", nullable = false, columnDefinition = "boolean default false not null")
    private boolean debeCambiarClave;

    /** Plan v3 (CU-16, CU-17): datos de la persona de la cuenta de personal. Nulos en cuentas antiguas. */
    @Column(name = "nombre_completo", length = 150)
    private String nombreCompleto;

    @Column(name = "documento", length = 20)
    private String documento;

    @Column(name = "correo", length = 100)
    private String correo;

    /** CU-26: cuando un tercero restablecio la clave. Los intentos fallidos anteriores ya no bloquean la cuenta. */
    @Column(name = "clave_restablecida_en")
    private LocalDateTime claveRestablecidaEn;

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
    /** RN-12: se usa para migrar el hash antiguo (SHA-256) a BCrypt al iniciar sesion. */
    public void actualizarContrasenaHash(String nuevoHash) { this.contrasenaHash = nuevoHash; }
    public String getEstado() { return estado; }
    public LocalDateTime getUltimoAcceso() { return ultimoAcceso; }
    public void setUltimoAcceso(LocalDateTime ultimoAcceso) { this.ultimoAcceso = ultimoAcceso; }
    public String getNombreCompleto() { return nombreCompleto; }
    public String getDocumento() { return documento; }
    public String getCorreo() { return correo; }
    /** CU-16, CU-17: datos de la persona. */
    public void setDatosPersona(String nombreCompleto, String documento, String correo) {
        this.nombreCompleto = nombreCompleto;
        this.documento = documento;
        this.correo = correo;
    }
    public LocalDateTime getClaveRestablecidaEn() { return claveRestablecidaEn; }
    /** CU-26, RN-14: nueva clave temporal puesta por un tercero; obliga a cambiarla y levanta el bloqueo por intentos. */
    public void restablecerClave(String nuevoHash, LocalDateTime cuando) {
        this.contrasenaHash = nuevoHash;
        this.debeCambiarClave = true;
        this.claveRestablecidaEn = cuando;
    }
    public void setEstado(String estado) { this.estado = estado; }
    public boolean isDebeCambiarClave() { return debeCambiarClave; }
    /** RN-14: marca o quita la obligacion de cambiar la clave temporal. */
    public void setDebeCambiarClave(boolean debeCambiarClave) { this.debeCambiarClave = debeCambiarClave; }
}
