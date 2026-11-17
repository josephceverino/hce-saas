package pe.edu.ucv.hce.persistencia;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.ucv.hce.dominio.SignosVitales;

public interface SignosVitalesRepository extends JpaRepository<SignosVitales, Integer> {
    /** CU-21: los signos vitales de una admision. */
    boolean existsByAdmisionId(Integer idAdmision);

    /** P-04: todos los signos vitales de una historia, el mas reciente primero. */
    List<SignosVitales> findAllByAdmisionHistoriaClinicaIdOrderByFechaHoraDesc(Integer idHistoria);
}
