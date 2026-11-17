package pe.edu.ucv.hce.aplicacion;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import pe.edu.ucv.hce.dominio.Cie10;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Catalogo CIE-10 (RN-10, DEC-04). Responsable: Mattias (tarea M-4).
 * Se carga de src/main/resources/cie10_demo.csv (se ignoran las lineas con #). Subconjunto de demostracion.
 * Si el CSV no se puede leer o queda vacio, la app no arranca: un catalogo vacio rechazaria todo diagnostico (RN-10).
 */
@Service
public class CatalogoCie10Service {

    private final List<Cie10> catalogo = new ArrayList<>();

    @PostConstruct
    public void cargarCatalogo() {
        try {
            ClassPathResource resource = new ClassPathResource("cie10_demo.csv");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String linea;
                while ((linea = reader.readLine()) != null) {
                    linea = linea.trim();
                    if (linea.isEmpty() || linea.startsWith("#")) {
                        continue;
                    }
                    // Asumimos formato CSV: codigo,descripcion (ignorando posible cabecera si contiene "codigo")
                    String[] partes = linea.split(",", 2);
                    if (partes.length == 2) {
                        String codigo = partes[0].trim();
                        String descripcion = partes[1].trim();
                        // El CSV usa comillas cuando la descripcion lleva comas: se quitan las comillas de los extremos.
                        if (descripcion.length() >= 2 && descripcion.startsWith("\"") && descripcion.endsWith("\"")) {
                            descripcion = descripcion.substring(1, descripcion.length() - 1).trim();
                        }
                        if (!codigo.equalsIgnoreCase("codigo")) {
                            catalogo.add(new Cie10(codigo, descripcion));
                        }
                    }
                }
            }
        } catch (java.io.IOException e) {
            throw new IllegalStateException("No se pudo leer el catálogo CIE-10 (cie10_demo.csv).", e);
        }
        if (catalogo.isEmpty()) {
            throw new IllegalStateException("El catálogo CIE-10 (cie10_demo.csv) está vacío.");
        }
    }

    public List<Cie10> buscar(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return List.of();
        }
        String filtro = normalizar(texto.trim());
        return catalogo.stream()
                .filter(item -> normalizar(item.codigo()).contains(filtro) ||
                                normalizar(item.descripcion()).contains(filtro))
                .limit(20)
                .collect(Collectors.toList());
    }

    public Optional<Cie10> porCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return Optional.empty();
        }
        return catalogo.stream()
                .filter(item -> item.codigo().equalsIgnoreCase(codigo.trim()))
                .findFirst();
    }

    private String normalizar(String input) {
        String nfd = Normalizer.normalize(input, Normalizer.Form.NFD);
        return nfd.replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase();
    }
}
