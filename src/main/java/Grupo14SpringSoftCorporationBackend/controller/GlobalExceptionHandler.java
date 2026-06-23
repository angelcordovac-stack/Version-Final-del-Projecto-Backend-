package Grupo14SpringSoftCorporationBackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manejador global de errores.
 * Convierte excepciones en respuestas JSON limpias para el frontend.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Las denegaciones de @PreAuthorize lanzan AccessDeniedException.
     * Sin este manejador explicito, el catch-all de Exception.class de mas
     * abajo la intercepta primero y la convierte incorrectamente en un 500,
     * en lugar del 403 que Spring Security normalmente generaria.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 403);
        body.put("error", "No tiene permisos para realizar esta accion");
        return ResponseEntity.status(403).body(body);
    }

    /**
     * Falta un @RequestParam obligatorio (ej. ?keyword=, ?password=).
     * Sin este manejador explicito, el catch-all de Exception.class la
     * convierte incorrectamente en un 500 en lugar de un 400.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParameter(MissingServletRequestParameterException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 400);
        body.put("error", "Falta el parametro obligatorio: " + ex.getParameterName());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", ex.getStatusCode().value());
        body.put("error", ex.getReason());
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    /**
     * Maneja errores de validacion de Bean Validation (@Valid, @NotBlank, @Email, etc.).
     * Devuelve un 400 con el detalle de cada campo invalido.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> erroresCampos = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Valor inválido",
                        (existing, replacement) -> existing // si hay duplicados, quedar con el primero
                ));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 400);
        body.put("error", "Error de validación");
        body.put("campos", erroresCampos);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 500);
        body.put("error", "Error inesperado: " + ex.getMessage());
        return ResponseEntity.status(500).body(body);
    }
}
