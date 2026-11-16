package pe.edu.ucv.hce.aplicacion;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import pe.edu.ucv.hce.dominio.Cie10;

/**
 * Catalogo CIE-10 (RN-10, DEC-04). Responsable: Mattias (tarea M-4).
 * CONTRATO: se carga de src/main/resources/cie10_demo.csv (ignorar lineas con #). Subconjunto de demostracion.
 */
@Service
public class CatalogoCie10Service {

    /** Busca por codigo o por texto de la descripcion (sin distinguir mayusculas ni tildes). Maximo 20 resultados. */
    public List<Cie10> buscar(String texto) {
        throw new UnsupportedOperationException("Pendiente: M-4 (Mattias)");
    }

    /** Devuelve la entrada si el codigo existe en el catalogo (RN-10); vacio si no. */
    public Optional<Cie10> porCodigo(String codigo) {
        throw new UnsupportedOperationException("Pendiente: M-4 (Mattias)");
    }
}
