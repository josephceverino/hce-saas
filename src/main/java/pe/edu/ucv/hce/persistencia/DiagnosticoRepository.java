package pe.edu.ucv.hce.persistencia;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.ucv.hce.dominio.Diagnostico;

public interface DiagnosticoRepository extends JpaRepository<Diagnostico, Integer> {
    /** RF-19: diagnosticos mas frecuentes del hospital. Cada fila: [codigoCie10, descripcion, total]. */
    @Query("select d.codigoCie10, d.descripcion, count(d) from Diagnostico d "
         + "where d.atencion.establecimiento.id = :hospital "
         + "group by d.codigoCie10, d.descripcion order by count(d) desc, d.codigoCie10")
    List<Object[]> masFrecuentes(@Param("hospital") Integer idHospital, Pageable limite);

    List<Diagnostico> findAllByAtencionIdOrderByIdAsc(Integer idAtencion);
}
