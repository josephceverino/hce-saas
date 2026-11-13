package pe.edu.ucv.hce.presentacion;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pe.edu.ucv.hce.dominio.DocumentoDuplicadoException;
import pe.edu.ucv.hce.dominio.NoAutenticadoException;

/** Convierte los errores en mensajes claros para el formulario. */
@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(DocumentoDuplicadoException.class)
    public ResponseEntity<Map<String, String>> duplicado(DocumentoDuplicadoException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensaje", e.getMessage()));
    }

    @ExceptionHandler(NoAutenticadoException.class)
    public ResponseEntity<Map<String, String>> noAutenticado(NoAutenticadoException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("mensaje", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> invalido(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> validacion(MethodArgumentNotValidException e) {
        String mensaje = e.getBindingResult().getFieldErrors().stream()
            .map(f -> f.getDefaultMessage()).findFirst().orElse("Datos invalidos.");
        return ResponseEntity.badRequest().body(Map.of("mensaje", mensaje));
    }
}
