package pe.edu.ucv.hce.persistencia;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.ucv.hce.dominio.Alergia;

public interface AlergiaRepository extends JpaRepository<Alergia, Integer> {
    List<Alergia> findAllByHistoriaClinicaId(Integer idHistoria);
}
