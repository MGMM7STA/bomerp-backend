package pe.edu.upeu.bomerp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private Map<String, Object> cuerpo(HttpStatus status, String error, String mensaje) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", mensaje);
        return body;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(cuerpo(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(cuerpo(HttpStatus.BAD_REQUEST, "Bad Request", "Error de validacion en los datos enviados"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(cuerpo(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage()));
    }

    @ExceptionHandler(StockInsuficienteException.class)
    public ResponseEntity<Map<String, Object>> handleStockInsuficiente(StockInsuficienteException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(cuerpo(HttpStatus.CONFLICT, "Conflict", ex.getMessage()));
    }

    @ExceptionHandler(CupoDiarioExcedidoException.class)
    public ResponseEntity<Map<String, Object>> handleCupoDiarioExcedido(CupoDiarioExcedidoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(cuerpo(HttpStatus.CONFLICT, "Conflict", ex.getMessage()));
    }

    @ExceptionHandler(EstadoRegistroInvalidoException.class)
    public ResponseEntity<Map<String, Object>> handleEstadoRegistroInvalido(EstadoRegistroInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(cuerpo(HttpStatus.CONFLICT, "Conflict", ex.getMessage()));
    }

    @ExceptionHandler(ReferenciaEnUsoException.class)
    public ResponseEntity<Map<String, Object>> handleReferenciaEnUso(ReferenciaEnUsoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(cuerpo(HttpStatus.CONFLICT, "Conflict", ex.getMessage()));
    }
    @ExceptionHandler(VentaYaAnuladaException.class)
    public ResponseEntity<Map<String, Object>> handleVentaYaAnulada(VentaYaAnuladaException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "Conflict");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(RegistroYaAnuladoException.class)
    public ResponseEntity<Map<String, Object>> handleRegistroYaAnulado(RegistroYaAnuladoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(cuerpo(HttpStatus.CONFLICT, "Conflict", ex.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleArchivoMuyGrande(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE)
                .body(cuerpo(HttpStatus.CONTENT_TOO_LARGE, "Content Too Large",
                        "Las fotos pesan demasiado: cada una debe pesar menos de 5 MB"));
    }

    @ExceptionHandler(ServicioExternoException.class)
    public ResponseEntity<Map<String, Object>> handleServicioExterno(ServicioExternoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(cuerpo(HttpStatus.BAD_GATEWAY, "Bad Gateway", ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleSinAcceso(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(cuerpo(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage()));
    }
}
