package pe.edu.ucv.hce.persistencia;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.ucv.hce.dominio.Enmienda;

public interface EnmiendaRepository extends JpaRepository<Enmienda, Integer> {
    List<Enmienda> findAllByAtencionIdOrderByFechaAsc(Integer idAtencion);
}
