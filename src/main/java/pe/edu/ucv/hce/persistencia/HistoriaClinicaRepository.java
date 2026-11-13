package pe.edu.ucv.hce.persistencia;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.ucv.hce.dominio.HistoriaClinica;

public interface HistoriaClinicaRepository extends JpaRepository<HistoriaClinica, Integer> {
    /** RN-06: cada hospital solo ve las historias que abrio. */
    List<HistoriaClinica> findAllByEstablecimientoIdOrderByIdDesc(Integer idEstablecimiento);

    /** CU-03 / RN-01: busca la historia unica de un documento (el paciente es global). */
    Optional<HistoriaClinica> findByPacienteTipoDocumentoAndPacienteNroDocumento(String tipoDocumento, String nroDocumento);
}
