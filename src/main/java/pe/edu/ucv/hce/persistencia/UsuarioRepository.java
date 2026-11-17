package pe.edu.ucv.hce.persistencia;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.ucv.hce.dominio.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByUsuario(String usuario);

    /** Plan v3, 0.2: para saber si ya existe un administrador de plataforma. */
    boolean existsByRolNombre(String nombreRol);

    /** RF-20: total de usuarios de un hospital (solo el numero). */
    long countByEstablecimientoId(Integer idEstablecimiento);

    /** CU-17 (RN-06, RN-16): el personal de un hospital, por nombre de usuario. */
    java.util.List<Usuario> findAllByEstablecimientoIdOrderByUsuarioAsc(Integer idEstablecimiento);

    /** CU-17 (RN-16): un usuario solo si es de ese hospital. */
    Optional<Usuario> findByIdAndEstablecimientoId(Integer id, Integer idEstablecimiento);
}
