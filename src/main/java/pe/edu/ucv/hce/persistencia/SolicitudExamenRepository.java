package pe.edu.ucv.hce.persistencia;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.ucv.hce.dominio.SolicitudExamen;

public interface SolicitudExamenRepository extends JpaRepository<SolicitudExamen, Integer> {

    /** CU-22: examenes de una atencion, en el orden en que se pidieron. */
    List<SolicitudExamen> findAllByAtencionIdOrderByIdAsc(Integer idAtencion);

    /** CU-23 (RN-06): las solicitudes de un hospital en un estado, las mas antiguas primero. */
    List<SolicitudExamen> findAllByEstablecimientoIdAndEstadoOrderByFechaHoraAsc(Integer idEstablecimiento, String estado);

    /** CU-23 (RN-06, RN-18): una solicitud solo dentro de su hospital. */
    Optional<SolicitudExamen> findByIdAndEstablecimientoId(Integer id, Integer idEstablecimiento);

    long countByEstablecimientoIdAndEstado(Integer idEstablecimiento, String estado);
}
