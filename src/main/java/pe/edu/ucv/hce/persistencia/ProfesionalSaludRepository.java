package pe.edu.ucv.hce.persistencia;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.ucv.hce.dominio.ProfesionalSalud;

public interface ProfesionalSaludRepository extends JpaRepository<ProfesionalSalud, Integer> {
    Optional<ProfesionalSalud> findByUsuarioId(Integer idUsuario);
}
