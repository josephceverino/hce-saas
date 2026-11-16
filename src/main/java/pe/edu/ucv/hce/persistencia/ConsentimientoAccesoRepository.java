package pe.edu.ucv.hce.persistencia;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.ucv.hce.dominio.ConsentimientoAcceso;

public interface ConsentimientoAccesoRepository extends JpaRepository<ConsentimientoAcceso, Integer> {
    /** CU-11: los consentimientos de un paciente, el mas reciente primero. */
    List<ConsentimientoAcceso> findAllByPacienteIdOrderByFechaOtorgadoDesc(Integer idPaciente);

    /** RN-04: busca un consentimiento solo si es de ese paciente. */
    Optional<ConsentimientoAcceso> findByIdAndPacienteId(Integer id, Integer idPaciente);
}
