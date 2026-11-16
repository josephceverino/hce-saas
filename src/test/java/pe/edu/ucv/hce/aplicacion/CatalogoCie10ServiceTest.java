package pe.edu.ucv.hce.aplicacion;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/** CONTRATO del catalogo CIE-10 (RN-10, DEC-04). Lo activa Mattias en la tarea M-4. */
@Disabled("Contrato: se activa en la tarea M-4 (catalogo CIE-10)")
class CatalogoCie10ServiceTest {

    /** Carga cie10_demo.csv ignorando las lineas que empiezan con # y la cabecera. */
    @Test
    void cargaElCatalogoDelCsv() { fail("Pendiente"); }

    /** porCodigo devuelve la entrada si existe (sin importar mayusculas) y vacio si no existe. */
    @Test
    void buscaPorCodigoExacto() { fail("Pendiente"); }

    /** buscar encuentra por parte del codigo o de la descripcion, sin distinguir mayusculas ni tildes. */
    @Test
    void buscaPorTexto() { fail("Pendiente"); }

    /** buscar devuelve como maximo 20 resultados y lista vacia si el texto esta en blanco. */
    @Test
    void limitaLosResultados() { fail("Pendiente"); }
}
