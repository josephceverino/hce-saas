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
import pe.edu.ucv.hce.aplicacion.CuentaService;
import pe.edu.ucv.hce.aplicacion.MenuService;
import pe.edu.ucv.hce.dominio.NoAutenticadoException;
import pe.edu.ucv.hce.dominio.Usuario;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AutenticacionService autenticacion;
    private final SesionActual sesionActual;
    private final MenuService menu;
    private final CuentaService cuentas;

    public AuthController(AutenticacionService autenticacion, SesionActual sesionActual, MenuService menu,
                          CuentaService cuentas) {
        this.cuentas = cuentas;
        this.autenticacion = autenticacion;
        this.sesionActual = sesionActual;
        this.menu = menu;
    }

    @PostMapping("/login")
    public SesionResponse login(@Valid @RequestBody LoginRequest datos, HttpServletRequest peticion) {
        Usuario usuario = autenticacion.iniciarSesion(datos.usuario().trim(), datos.clave(), peticion.getRemoteAddr());
        HttpSession anterior = peticion.getSession(false);
        if (anterior != null) {
            anterior.invalidate();
        }
        HttpSession sesion = peticion.getSession(true);
        sesion.setAttribute(SesionActual.ATRIBUTO, usuario.getId());
        return SesionResponse.de(usuario, menu.menuPara(usuario));
    }

    @PostMapping("/logout")
    public void logout(HttpSession sesion, HttpServletRequest peticion) {
        try {
            autenticacion.cerrarSesion(sesionActual.requerirAunqueDebaCambiarClave(sesion), peticion.getRemoteAddr());
        } catch (NoAutenticadoException e) {
            // sin sesion valida: no hay nada que auditar, solo se invalida
        }
        sesion.invalidate();
    }

    @GetMapping("/sesion")
    public SesionResponse sesion(HttpSession sesion) {
        Usuario usuario = sesionActual.requerirAunqueDebaCambiarClave(sesion);
        return SesionResponse.de(usuario, menu.menuPara(usuario));
    }

    /** CU-18 (RF-28; RN-14, RN-12): cambio de clave, obligatorio si la clave es temporal. */
    @PostMapping("/sesion/clave")
    public SesionResponse cambiarClave(@Valid @RequestBody CambioClaveRequest datos, HttpSession sesion,
                                       HttpServletRequest peticion) {
        Usuario usuario = sesionActual.requerirAunqueDebaCambiarClave(sesion);
        Usuario actualizado = cuentas.cambiarClave(usuario, datos.claveActual(), datos.claveNueva(), peticion.getRemoteAddr());
        return SesionResponse.de(actualizado, menu.menuPara(actualizado));
    }
}
