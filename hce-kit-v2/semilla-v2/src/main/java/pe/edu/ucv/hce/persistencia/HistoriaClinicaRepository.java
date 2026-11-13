package pe.edu.ucv.hce.persistencia;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.ucv.hce.dominio.HistoriaClinica;

public interface HistoriaClinicaRepository extends JpaRepository<HistoriaClinica, Integer> {
    /** RN-06: cada hospital solo ve las historias que abrio. */
    List<HistoriaClinica> findAllByEstablecimientoIdOrderByIdDesc(Integer idEstablecimiento);
}
