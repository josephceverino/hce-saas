package pe.edu.ucv.hce.aplicacion;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ucv.hce.dominio.NoAutenticadoException;
import pe.edu.ucv.hce.dominio.Usuario;
import pe.edu.ucv.hce.persistencia.UsuarioRepository;

/** CU-01: iniciar sesion (version de demostracion). */
@Service
public class AutenticacionService {

    private final UsuarioRepository usuarios;

    public AutenticacionService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Transactional
    public Usuario iniciarSesion(String nombreUsuario, String clave) {
        Usuario u = usuarios.findByUsuario(nombreUsuario)
            .orElseThrow(() -> new NoAutenticadoException("Usuario o clave incorrectos."));
        if (!"ACTIVO".equals(u.getEstado()) || !ClaveUtil.coincide(clave, u.getContrasenaHash())) {
            throw new NoAutenticadoException("Usuario o clave incorrectos.");
        }
        u.setUltimoAcceso(LocalDateTime.now());
        return u;
    }
}
