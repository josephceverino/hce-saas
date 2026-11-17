package pe.edu.ucv.hce.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.ucv.hce.dominio.ResultadoExamen;

public interface ResultadoExamenRepository extends JpaRepository<ResultadoExamen, Integer> {
}
