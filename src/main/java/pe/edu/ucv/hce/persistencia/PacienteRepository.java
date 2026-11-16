package pe.edu.ucv.hce.persistencia;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.ucv.hce.dominio.Paciente;

public interface PacienteRepository extends JpaRepository<Paciente, Integer> {
    boolean existsByTipoDocumentoAndNroDocumento(String tipoDocumento, String nroDocumento);

    /** CU-10 / RN-04: el paciente dueño de una cuenta del portal. */
    Optional<Paciente> findByUsuarioId(Integer idUsuario);

    Optional<Paciente> findByTipoDocumentoAndNroDocumento(String tipoDocumento, String nroDocumento);
}
