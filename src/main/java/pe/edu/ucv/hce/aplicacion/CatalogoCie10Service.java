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
                        if (!codigo.equalsIgnoreCase("codigo")) {
                            catalogo.add(new Cie10(codigo, descripcion));
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Si hay error al leer el recurso en test, se mantiene vacío o maneja de forma segura
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
