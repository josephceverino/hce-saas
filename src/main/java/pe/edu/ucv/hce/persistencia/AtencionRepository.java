package pe.edu.ucv.hce.persistencia;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.ucv.hce.dominio.Atencion;

public interface AtencionRepository extends JpaRepository<Atencion, Integer> {
    // Indicadores (RF-19): siempre filtrados por hospital (RN-06). Cifras reales, nunca inventadas.
    long countByEstablecimientoId(Integer idEstablecimiento);
    long countByEstablecimientoIdAndEstado(Integer idEstablecimiento, String estado);
    long countByEstablecimientoIdAndFechaHoraGreaterThanEqual(Integer idEstablecimiento, LocalDateTime desde);
    long countByProfesionalIdAndFechaHoraGreaterThanEqual(Integer idProfesional, LocalDateTime desde);
    long countByProfesionalIdAndEstado(Integer idProfesional, String estado);
    List<Atencion> findAllByEstablecimientoIdAndFechaHoraGreaterThanEqual(Integer idEstablecimiento, LocalDateTime desde);

    /** CU-06: atenciones de una historia, la mas reciente primero. */
    List<Atencion> findAllByHistoriaClinicaIdOrderByFechaHoraDesc(Integer idHistoria);

    /** RN-06: busca una atencion solo dentro del hospital del usuario. */
    Optional<Atencion> findByIdAndEstablecimientoId(Integer id, Integer idEstablecimiento);
}
