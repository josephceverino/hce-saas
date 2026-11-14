package pe.edu.ucv.hce.persistencia;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.ucv.hce.dominio.RegistroAuditoria;

public interface RegistroAuditoriaRepository extends JpaRepository<RegistroAuditoria, Integer> {
    /** RN-07: registros de una historia, del mas reciente al mas antiguo. */
    List<RegistroAuditoria> findAllByHistoriaClinicaIdOrderByIdDesc(Integer idHistoriaClinica);
}
