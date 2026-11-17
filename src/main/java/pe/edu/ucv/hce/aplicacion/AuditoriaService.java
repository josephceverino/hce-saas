package pe.edu.ucv.hce.aplicacion;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ucv.hce.dominio.AccesoDenegadoException;
import pe.edu.ucv.hce.dominio.HistoriaClinica;
import pe.edu.ucv.hce.dominio.RegistroAuditoria;
import pe.edu.ucv.hce.dominio.Usuario;
import pe.edu.ucv.hce.persistencia.RegistroAuditoriaRepository;

/**
 * Auditoria transversal minima (DD-04, DEC-06; RN-07, RNF-08, RF-13).
 * Se invoca desde CU-03 (y en v3 desde CU-05, CU-06 y CU-08). En v5 se completa (pantalla, filtros, cobertura total).
 * Corre en la misma transaccion del caso de uso: si la operacion falla, tampoco queda el registro.
 */
@Service
public class AuditoriaService {

    public static final String IP_DESCONOCIDA = "desconocida";

    public static final int TAMANO_MAXIMO = 100;

    private final RegistroAuditoriaRepository registros;
    private final AutorizacionService autorizacion;

    public AuditoriaService(RegistroAuditoriaRepository registros, AutorizacionService autorizacion) {
        this.registros = registros;
        this.autorizacion = autorizacion;
    }

    /**
     * Consulta de auditoria del Administrador del hospital (RF-13, RN-07, RNF-08; multi-tenant RN-06).
     * Solo ve registros de SU hospital: hechos por usuarios de su hospital o que tocaron historias de su hospital
     * (asi tambien aparece cuando un paciente consulta su historia). Filtros opcionales; pagina desde 0.
     */
    @Transactional(readOnly = true)
    public PaginaAuditoria consultar(Usuario solicitante, FiltroAuditoria filtro, int pagina, int tamano) {
        autorizacion.exigir(solicitante, Permiso.VER_AUDITORIA);
        Integer hospital = hospitalDe(solicitante);
        int p = Math.max(pagina, 0);
        int t = Math.min(Math.max(tamano, 1), TAMANO_MAXIMO);
        Page<RegistroAuditoria> r = registros.findAll(especificacion(hospital, filtro),
            PageRequest.of(p, t, Sort.by(Sort.Direction.DESC, "id")));
        return new PaginaAuditoria(r.getContent(), p, t, r.getTotalElements(), r.getTotalPages());
    }

    /** Acciones que existen en el hospital, para llenar el filtro de la pantalla. */
    @Transactional(readOnly = true)
    public List<String> accionesDelHospital(Usuario solicitante) {
        autorizacion.exigir(solicitante, Permiso.VER_AUDITORIA);
        return registros.accionesDelHospital(hospitalDe(solicitante));
    }

    private Integer hospitalDe(Usuario u) {
        if (u.getEstablecimiento() == null) {
            throw new AccesoDenegadoException("Tu usuario no pertenece a un hospital.");
        }
        return u.getEstablecimiento().getId();
    }

    private Specification<RegistroAuditoria> especificacion(Integer hospital, FiltroAuditoria f) {
        return (root, query, cb) -> {
            var usuario = root.join("usuario", JoinType.LEFT);
            var historia = root.join("historiaClinica", JoinType.LEFT);
            List<Predicate> condiciones = new ArrayList<>();
            // RN-06: solo lo del hospital del solicitante
            condiciones.add(cb.or(
                cb.equal(usuario.get("establecimiento").get("id"), hospital),
                cb.equal(historia.get("establecimiento").get("id"), hospital)));
            if (f != null) {
                if (hayTexto(f.usuario())) {
                    condiciones.add(cb.like(cb.lower(usuario.get("usuario")), "%" + f.usuario().trim().toLowerCase() + "%"));
                }
                if (hayTexto(f.accion())) {
                    condiciones.add(cb.equal(root.get("accion"), f.accion().trim()));
                }
                if (hayTexto(f.nroHc())) {
                    condiciones.add(cb.equal(historia.get("nroHc"), f.nroHc().trim()));
                }
                if (f.desde() != null) {
                    condiciones.add(cb.greaterThanOrEqualTo(root.get("fechaHora"), f.desde().atStartOfDay()));
                }
                if (f.hasta() != null) {
                    condiciones.add(cb.lessThan(root.get("fechaHora"), f.hasta().plusDays(1).atStartOfDay()));
                }
            }
            return cb.and(condiciones.toArray(new Predicate[0]));
        };
    }

    private static boolean hayTexto(String s) {
        return s != null && !s.isBlank();
    }

    @Transactional
    public RegistroAuditoria registrar(Usuario usuario, String accion, String entidadAfectada,
                                       Integer idEntidadAfectada, HistoriaClinica historia, String ip) {
        String origen = (ip == null || ip.isBlank()) ? IP_DESCONOCIDA : ip;
        return registros.save(new RegistroAuditoria(usuario, historia, LocalDateTime.now(),
            accion, entidadAfectada, idEntidadAfectada, origen));
    }
}
