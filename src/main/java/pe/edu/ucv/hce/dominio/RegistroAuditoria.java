package pe.edu.ucv.hce.dominio;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Registro de auditoria (tabla registro_auditoria; RN-07, RNF-08, RF-13, DEC-06).
 * entidad_afectada e id_entidad_afectada van sin FK a proposito (docs/modelo_datos.md).
 * El establecimiento no tiene columna propia en el modelo: se obtiene del usuario (usuario.id_establecimiento).
 * Un registro de auditoria no se modifica: no tiene setters.
 */
@Entity
@Table(name = "registro_auditoria")
public class RegistroAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_registro_auditoria")
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_historia_clinica")
    private HistoriaClinica historiaClinica;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "accion", nullable = false, length = 100)
    private String accion;

    @Column(name = "entidad_afectada", nullable = false, length = 50)
    private String entidadAfectada;

    @Column(name = "id_entidad_afectada", nullable = false)
    private Integer idEntidadAfectada;

    @Column(name = "ip", nullable = false, length = 45)
    private String ip;

    protected RegistroAuditoria() { }

    public RegistroAuditoria(Usuario usuario, HistoriaClinica historiaClinica, LocalDateTime fechaHora,
                             String accion, String entidadAfectada, Integer idEntidadAfectada, String ip) {
        this.usuario = usuario;
        this.historiaClinica = historiaClinica;
        this.fechaHora = fechaHora;
        this.accion = accion;
        this.entidadAfectada = entidadAfectada;
        this.idEntidadAfectada = idEntidadAfectada;
        this.ip = ip;
    }

    public Integer getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public HistoriaClinica getHistoriaClinica() { return historiaClinica; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public String getAccion() { return accion; }
    public String getEntidadAfectada() { return entidadAfectada; }
    public Integer getIdEntidadAfectada() { return idEntidadAfectada; }
    public String getIp() { return ip; }
}
