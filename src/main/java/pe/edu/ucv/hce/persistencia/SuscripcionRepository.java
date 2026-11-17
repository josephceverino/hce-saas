package pe.edu.ucv.hce.persistencia;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.ucv.hce.dominio.Suscripcion;

public interface SuscripcionRepository extends JpaRepository<Suscripcion, Integer> {
    /** CU-16: la suscripcion vigente (la mas reciente) de un hospital. */
    Optional<Suscripcion> findFirstByEstablecimientoIdOrderByFechaInicioDescIdDesc(Integer idEstablecimiento);
}
