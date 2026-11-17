package pe.edu.ucv.hce.presentacion;

import java.time.LocalDate;
import java.time.LocalDateTime;
import pe.edu.ucv.hce.dominio.Admision;
import pe.edu.ucv.hce.dominio.Paciente;

/** Una admision tal como la ve la cola (RF-22). Solo datos del hospital del usuario (RN-06). */
public record AdmisionDto(Integer id, Integer idHistoriaClinica, String nroHc, String paciente, String tipoDocumento,
                          String nroDocumento, LocalDate fechaNacimiento, String sexo, String tipoAtencion,
                          String estado, String estadoDescripcion, LocalDateTime fechaHoraAdmision,
                          LocalDateTime fechaHoraEstado) {

    public static AdmisionDto de(Admision a) {
        Paciente p = a.getHistoriaClinica().getPaciente();
        return new AdmisionDto(a.getId(), a.getHistoriaClinica().getId(), a.getHistoriaClinica().getNroHc(),
            p.getApellidos() + ", " + p.getNombres(), p.getTipoDocumento(), p.getNroDocumento(), p.getFechaNacimiento(),
            p.getSexo(), a.getTipoAtencion(), a.getEstado().name(), a.getEstado().descripcion(),
            a.getFechaHoraAdmision(), a.getFechaHoraEstado());
    }
}
