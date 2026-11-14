package pe.edu.ucv.hce.persistencia;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.ucv.hce.dominio.Establecimiento;

public interface EstablecimientoRepository extends JpaRepository<Establecimiento, Integer> {
    Optional<Establecimiento> findFirstByEstado(String estado);
    Optional<Establecimiento> findByCodigoIpress(String codigoIpress);
}
