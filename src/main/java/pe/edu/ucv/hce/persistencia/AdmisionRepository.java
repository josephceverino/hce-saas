package pe.edu.ucv.hce.persistencia;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.ucv.hce.dominio.Admision;
import pe.edu.ucv.hce.dominio.EstadoAdmision;

public interface AdmisionRepository extends JpaRepository<Admision, Integer> {

    /** RN-06: una admision solo si es del hospital indicado. */
    Optional<Admision> findByIdAndEstablecimientoId(Integer id, Integer idEstablecimiento);

    /** RF-22: admisiones del hospital desde una hora (el dia), en los estados pedidos. */
    List<Admision> findAllByEstablecimientoIdAndFechaHoraAdmisionGreaterThanEqualAndEstadoIn(
        Integer idEstablecimiento, LocalDateTime desde, Collection<EstadoAdmision> estados);

    /** CU-20 alternativo: admisiones de una historia desde una hora (para detectar una activa hoy). */
    List<Admision> findAllByHistoriaClinicaIdAndFechaHoraAdmisionGreaterThanEqual(Integer idHistoria, LocalDateTime desde);
}
