package pe.edu.ucv.hce.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import pe.edu.ucv.hce.dominio.Cie10;

/** CONTRATO del catalogo CIE-10 (RN-10, DEC-04). Lo activa Mattias en la tarea M-4. */
@SpringBootTest
class CatalogoCie10ServiceTest {

    @Autowired
    private CatalogoCie10Service service;

    /** Carga cie10_demo.csv ignorando las lineas que empiezan con # y la cabecera. */
    @Test
    void cargaElCatalogoDelCsv() {
        List<Cie10> resultados = service.buscar("Neumonia");
        assertFalse(resultados.isEmpty());
    }

    /** porCodigo devuelve la entrada si existe (sin importar mayusculas) y vacio si no existe. */
    @Test
    void buscaPorCodigoExacto() {
        Optional<Cie10> item = service.porCodigo("j00");
        assertTrue(item.isPresent());
        assertEquals("J00", item.get().codigo());
        Optional<Cie10> noExiste = service.porCodigo("XYZ.9");
        assertFalse(noExiste.isPresent());
    }

    /** buscar encuentra por parte del codigo o de la descripcion, sin distinguir mayusculas ni tildes. */
    @Test
    void buscaPorTexto() {
        List<Cie10> resultados = service.buscar("gastritis");
        assertFalse(resultados.isEmpty());
    }

    /** buscar devuelve como maximo 20 resultados y lista vacia si el texto esta en blanco. */
    @Test
    void limitaLosResultados() {
        List<Cie10> vacios = service.buscar("   ");
        assertTrue(vacios.isEmpty());

        List<Cie10> resultados = service.buscar("a");
        assertTrue(resultados.size() <= 20);
    }

    /** Las descripciones con comas vienen entre comillas en el CSV: no deben quedar con comillas (RN-10). */
    @Test
    void laDescripcionNoLlevaComillas() {
        Cie10 item = service.porCodigo("J02.9").orElseThrow();
        assertEquals("Faringitis aguda, no especificada", item.descripcion());
    }
}
