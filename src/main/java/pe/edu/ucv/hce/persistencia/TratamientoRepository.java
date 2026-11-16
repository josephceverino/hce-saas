package pe.edu.ucv.hce.persistencia;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.ucv.hce.dominio.Tratamiento;

public interface TratamientoRepository extends JpaRepository<Tratamiento, Integer> {
    List<Tratamiento> findAllByAtencionIdOrderByIdAsc(Integer idAtencion);
}
