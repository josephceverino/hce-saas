package pe.edu.ucv.hce.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.ucv.hce.dominio.Paciente;

public interface PacienteRepository extends JpaRepository<Paciente, Integer> {
    boolean existsByTipoDocumentoAndNroDocumento(String tipoDocumento, String nroDocumento);
}
