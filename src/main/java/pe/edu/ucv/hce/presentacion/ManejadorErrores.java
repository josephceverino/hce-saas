package pe.edu.ucv.hce.presentacion;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pe.edu.ucv.hce.dominio.AccesoDenegadoException;
import pe.edu.ucv.hce.dominio.AtencionCerradaException;
import pe.edu.ucv.hce.dominio.DiagnosticoInvalidoException;
import pe.edu.ucv.hce.dominio.DocumentoDuplicadoException;
import pe.edu.ucv.hce.dominio.NoAutenticadoException;
import pe.edu.ucv.hce.dominio.RecursoNoEncontradoException;

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

    /** RN-02, DEC-03: el rol no tiene permiso. */
    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<Map<String, String>> sinPermiso(AccesoDenegadoException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("mensaje", e.getMessage()));
    }

    /** RN-03: la atencion esta cerrada; solo se corrige con enmienda. */
    @ExceptionHandler(AtencionCerradaException.class)
    public ResponseEntity<Map<String, String>> atencionCerrada(AtencionCerradaException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensaje", e.getMessage()));
    }

    /** La historia o la atencion no existe. */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> noEncontrado(RecursoNoEncontradoException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", e.getMessage()));
    }

    /** RN-10: codigo fuera del catalogo CIE-10. */
    @ExceptionHandler(DiagnosticoInvalidoException.class)
    public ResponseEntity<Map<String, String>> diagnosticoInvalido(DiagnosticoInvalidoException e) {
        return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
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
