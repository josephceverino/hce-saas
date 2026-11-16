package pe.edu.ucv.hce.persistencia;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.ucv.hce.dominio.Diagnostico;

public interface DiagnosticoRepository extends JpaRepository<Diagnostico, Integer> {
    List<Diagnostico> findAllByAtencionIdOrderByIdAsc(Integer idAtencion);
}
