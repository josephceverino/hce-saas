package pe.edu.ucv.hce.persistencia;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.ucv.hce.dominio.HistoriaClinica;

public interface HistoriaClinicaRepository extends JpaRepository<HistoriaClinica, Integer> {
    // Indicadores (RF-19): filtrados por hospital (RN-06).
    long countByEstablecimientoId(Integer idEstablecimiento);
    long countByEstablecimientoIdAndEstado(Integer idEstablecimiento, String estado);
    long countByEstablecimientoIdAndFechaApertura(Integer idEstablecimiento, LocalDate fecha);
    List<HistoriaClinica> findAllByEstablecimientoIdAndFechaAperturaGreaterThanEqual(Integer idEstablecimiento, LocalDate desde);

    /** RN-06: cada hospital solo ve las historias que abrio. */
    List<HistoriaClinica> findAllByEstablecimientoIdOrderByIdDesc(Integer idEstablecimiento);

    /** CU-10 / RN-04: la historia de un paciente. */
    Optional<HistoriaClinica> findByPacienteId(Integer idPaciente);

    /** CU-03 / RN-01: busca la historia unica de un documento (el paciente es global). */
    Optional<HistoriaClinica> findByPacienteTipoDocumentoAndPacienteNroDocumento(String tipoDocumento, String nroDocumento);
}
