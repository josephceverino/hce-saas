package pe.edu.ucv.hce.presentacion;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.ucv.hce.aplicacion.AutenticacionService;
import pe.edu.ucv.hce.dominio.Usuario;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AutenticacionService autenticacion;
    private final SesionActual sesionActual;

    public AuthController(AutenticacionService autenticacion, SesionActual sesionActual) {
        this.autenticacion = autenticacion;
        this.sesionActual = sesionActual;
    }

    @PostMapping("/login")
    public SesionResponse login(@Valid @RequestBody LoginRequest datos, HttpServletRequest peticion) {
        Usuario usuario = autenticacion.iniciarSesion(datos.usuario().trim(), datos.clave());
        HttpSession anterior = peticion.getSession(false);
        if (anterior != null) {
            anterior.invalidate();
        }
        HttpSession sesion = peticion.getSession(true);
        sesion.setAttribute(SesionActual.ATRIBUTO, usuario.getId());
        return SesionResponse.de(usuario);
    }

    @PostMapping("/logout")
    public void logout(HttpSession sesion) {
        sesion.invalidate();
    }

    @GetMapping("/sesion")
    public SesionResponse sesion(HttpSession sesion) {
        return SesionResponse.de(sesionActual.requerir(sesion));
    }
}
