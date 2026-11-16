package pe.edu.ucv.hce.persistencia;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.ucv.hce.dominio.Antecedente;

public interface AntecedenteRepository extends JpaRepository<Antecedente, Integer> {
    List<Antecedente> findAllByHistoriaClinicaIdOrderByFechaRegistroDesc(Integer idHistoria);
}
