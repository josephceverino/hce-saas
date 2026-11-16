package pe.edu.ucv.hce.persistencia;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.ucv.hce.dominio.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByUsuario(String usuario);

    /** RF-20: total de usuarios de un hospital (solo el numero). */
    long countByEstablecimientoId(Integer idEstablecimiento);
}
