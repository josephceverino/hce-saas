package pe.edu.ucv.hce.persistencia;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.ucv.hce.dominio.RegistroAuditoria;

public interface RegistroAuditoriaRepository
        extends JpaRepository<RegistroAuditoria, Integer>, JpaSpecificationExecutor<RegistroAuditoria> {

    /** RN-07 / RF-13: acciones distintas que aparecen en el hospital (para el filtro de la pantalla de auditoria). */
    @Query("select distinct r.accion from RegistroAuditoria r "
         + "left join r.usuario u left join u.establecimiento ue "
         + "left join r.historiaClinica h left join h.establecimiento he "
         + "where ue.id = :hospital or he.id = :hospital order by r.accion")
    List<String> accionesDelHospital(@Param("hospital") Integer idHospital);

    /** RN-07: registros de una historia, del mas reciente al mas antiguo. */
    List<RegistroAuditoria> findAllByHistoriaClinicaIdOrderByIdDesc(Integer idHistoriaClinica);
}
