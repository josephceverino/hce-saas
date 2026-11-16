package pe.edu.ucv.hce.persistencia;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.ucv.hce.dominio.Atencion;

public interface AtencionRepository extends JpaRepository<Atencion, Integer> {
    /** CU-06: atenciones de una historia, la mas reciente primero. */
    List<Atencion> findAllByHistoriaClinicaIdOrderByFechaHoraDesc(Integer idHistoria);

    /** RN-06: busca una atencion solo dentro del hospital del usuario. */
    Optional<Atencion> findByIdAndEstablecimientoId(Integer id, Integer idEstablecimiento);
}
