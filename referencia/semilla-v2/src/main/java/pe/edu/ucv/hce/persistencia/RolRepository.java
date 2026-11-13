package pe.edu.ucv.hce.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.ucv.hce.dominio.Rol;

public interface RolRepository extends JpaRepository<Rol, Integer> {
}
