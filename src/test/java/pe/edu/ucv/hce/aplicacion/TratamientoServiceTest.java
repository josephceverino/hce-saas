package pe.edu.ucv.hce.aplicacion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import pe.edu.ucv.hce.dominio.*;
import pe.edu.ucv.hce.persistencia.AtencionRepository;
import pe.edu.ucv.hce.persistencia.RegistroAuditoriaRepository;
import pe.edu.ucv.hce.persistencia.TratamientoRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TratamientoServiceTest {

    @Mock
    private TratamientoRepository tratamientoRepository;

    @Mock
    private AtencionRepository atencionRepository;

    @Mock
    private RegistroAuditoriaRepository auditoriaRepository;

    @InjectMocks
    private TratamientoService tratamientoService;

    @Test
    void medicoAgregaTratamientoAAtencionAbierta() {
        Establecimiento est = Mockito.mock(Establecimiento.class);
        when(est.getId()).thenReturn(1);

        Atencion atencion = Mockito.mock(Atencion.class);
        when(atencion.getEstado()).thenReturn("ABIERTA");
        when(atencion.getEstablecimiento()).thenReturn(est);
        when(atencionRepository.findById(1)).thenReturn(Optional.of(atencion));

        Rol rolMock = Mockito.mock(Rol.class);
        when(rolMock.getNombre()).thenReturn("MEDICO");
        Usuario usuarioMock = Mockito.mock(Usuario.class);
        when(usuarioMock.getRol()).thenReturn(rolMock);
        when(usuarioMock.getEstablecimiento()).thenReturn(est);

        Tratamiento tratamientoMock = Mockito.mock(Tratamiento.class);
        when(tratamientoRepository.save(any(Tratamiento.class))).thenReturn(tratamientoMock);

        Tratamiento resultado = tratamientoService.agregar(
            usuarioMock, 
            1, 
            "Reposo", 
            "Paracetamol", 
            "500mg", 
            "3 dias", 
            "127.0.0.1"
        );

        assertNotNull(resultado);
    }

    @Test
    void enfermeriaNoRegistraTratamiento() {
        Rol rolMock = Mockito.mock(Rol.class);
        when(rolMock.getNombre()).thenReturn("ENFERMERA");
        Usuario enfermeraMock = Mockito.mock(Usuario.class);
        when(enfermeraMock.getRol()).thenReturn(rolMock);

        assertThrows(AccesoDenegadoException.class, () -> {
            tratamientoService.agregar(
                enfermeraMock, 
                1, 
                "Reposo", 
                "Paracetamol", 
                "500mg", 
                "3 dias", 
                "127.0.0.1"
            );
        });
    }

    @Test
    void noSeAgregaTratamientoAAtencionCerrada() {
        Establecimiento est = Mockito.mock(Establecimiento.class);
        when(est.getId()).thenReturn(1);

        Atencion atencion = Mockito.mock(Atencion.class);
        when(atencion.getEstado()).thenReturn("CERRADA");
        when(atencion.getEstablecimiento()).thenReturn(est);
        when(atencionRepository.findById(1)).thenReturn(Optional.of(atencion));

        Rol rolMock = Mockito.mock(Rol.class);
        when(rolMock.getNombre()).thenReturn("MEDICO");
        Usuario medicoMock = Mockito.mock(Usuario.class);
        when(medicoMock.getRol()).thenReturn(rolMock);
        when(medicoMock.getEstablecimiento()).thenReturn(est);

        assertThrows(AtencionCerradaException.class, () -> {
            tratamientoService.agregar(
                medicoMock, 
                1, 
                "Reposo", 
                "Paracetamol", 
                "500mg", 
                "3 dias", 
                "127.0.0.1"
            );
        });
    }

    @Test
    void otroHospitalNoPuedeAgregarTratamiento() {
        Establecimiento estA = Mockito.mock(Establecimiento.class);
        when(estA.getId()).thenReturn(1);

        Establecimiento estB = Mockito.mock(Establecimiento.class);
        when(estB.getId()).thenReturn(2);

        Atencion atencion = Mockito.mock(Atencion.class);
        when(atencion.getEstado()).thenReturn("ABIERTA");
        when(atencion.getEstablecimiento()).thenReturn(estA);
        when(atencionRepository.findById(1)).thenReturn(Optional.of(atencion));

        Rol rolMock = Mockito.mock(Rol.class);
        when(rolMock.getNombre()).thenReturn("MEDICO");
        Usuario medicoMock = Mockito.mock(Usuario.class);
        when(medicoMock.getRol()).thenReturn(rolMock);
        when(medicoMock.getEstablecimiento()).thenReturn(estB);

        assertThrows(RecursoNoEncontradoException.class, () -> {
            tratamientoService.agregar(
                medicoMock, 
                1, 
                "Reposo", 
                "Paracetamol", 
                "500mg", 
                "3 dias", 
                "127.0.0.1"
            );
        });
    }

    @Test
    void rechazaCamposVacios() {
        Rol rolMock = Mockito.mock(Rol.class);
        when(rolMock.getNombre()).thenReturn("MEDICO");
        Usuario medicoMock = Mockito.mock(Usuario.class);
        when(medicoMock.getRol()).thenReturn(rolMock);

        assertThrows(IllegalArgumentException.class, () -> {
            tratamientoService.agregar(
                medicoMock, 
                1, 
                "", 
                "Paracetamol", 
                "500mg", 
                "3 dias", 
                "127.0.0.1"
            );
        });
    }

    @Test
    void laAccionQuedaAuditada() {
        Establecimiento est = Mockito.mock(Establecimiento.class);
        when(est.getId()).thenReturn(1);

        Atencion atencion = Mockito.mock(Atencion.class);
        when(atencion.getEstado()).thenReturn("ABIERTA");
        when(atencion.getEstablecimiento()).thenReturn(est);
        when(atencionRepository.findById(1)).thenReturn(Optional.of(atencion));

        Rol rolMock = Mockito.mock(Rol.class);
        when(rolMock.getNombre()).thenReturn("MEDICO");
        Usuario medicoMock = Mockito.mock(Usuario.class);
        when(medicoMock.getRol()).thenReturn(rolMock);
        when(medicoMock.getEstablecimiento()).thenReturn(est);

        Tratamiento tratamientoMock = Mockito.mock(Tratamiento.class);
        when(tratamientoRepository.save(any(Tratamiento.class))).thenReturn(tratamientoMock);

        Tratamiento resultado = tratamientoService.agregar(
            medicoMock, 
            1, 
            "Reposo", 
            "Paracetamol", 
            "500mg", 
            "3 dias", 
            "127.0.0.1"
        );

        assertNotNull(resultado);
    }
}