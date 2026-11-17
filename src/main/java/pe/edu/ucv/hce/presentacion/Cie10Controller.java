package pe.edu.ucv.hce.presentacion;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.ucv.hce.aplicacion.AutorizacionService;
import pe.edu.ucv.hce.aplicacion.CatalogoCie10Service;
import pe.edu.ucv.hce.aplicacion.Permiso;
import pe.edu.ucv.hce.dominio.Cie10;
import pe.edu.ucv.hce.dominio.Usuario;

/** Busqueda de diagnosticos CIE-10 (RN-10, DEC-04). Solo MEDICO. Contrato: docs/contratos/atencion_historia.md, sec. 7. */
@RestController
@RequestMapping("/api/cie10")
public class Cie10Controller {

    private final CatalogoCie10Service catalogo;
    private final AutorizacionService autorizacion;
    private final SesionActual sesionActual;

    public Cie10Controller(CatalogoCie10Service catalogo, AutorizacionService autorizacion,
                           SesionActual sesionActual) {
        this.catalogo = catalogo;
        this.autorizacion = autorizacion;
        this.sesionActual = sesionActual;
    }

    @GetMapping
    public List<Cie10> buscar(@RequestParam(name = "q", defaultValue = "") String texto, HttpSession sesion) {
        Usuario usuario = sesionActual.requerir(sesion);
        autorizacion.exigir(usuario, Permiso.REGISTRAR_ATENCION);
        return catalogo.buscar(texto);
    }
}
