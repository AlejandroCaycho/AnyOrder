package com.anyorder.pos.anyorder.exception;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        error -> error.getField(),
                        error -> error.getDefaultMessage(),
                        (existing, replacement) -> existing + ", " + replacement));

        return buildError(
                "Validación fallida",
                errors.isEmpty() ? ex.getMessage() : errors,
                HttpStatus.BAD_REQUEST,
                "Los campos no cumplen con los requisitos especificados");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrity(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause().getMessage();

        return buildError(
                "Violación de integridad de datos",
                extractDetailedMessage(message),
                HttpStatus.BAD_REQUEST,
                message);
    }

    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity<?> handleEmptyResult(EmptyResultDataAccessException ex) {
        return buildError(
                "Registro no encontrado",
                "El ID solicitado no existe en la base de datos",
                HttpStatus.NOT_FOUND,
                ex.getMessage());
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<?> handleDatabase(DataAccessException ex) {
        Throwable cause = ex.getMostSpecificCause();
        String message = cause != null ? cause.getMessage() : ex.getMessage();

        return buildError(
                "Error de acceso a base de datos",
                message,
                HttpStatus.SERVICE_UNAVAILABLE,
                extractDetailedMessage(message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        return buildError(
                "Argumento inválido",
                ex.getMessage(),
                HttpStatus.BAD_REQUEST,
                ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntime(RuntimeException ex) {
        return buildError(
                "Error en tiempo de ejecución",
                ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                getStackTrace(ex));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneral(Exception ex) {
        return buildError(
                "Error inesperado del sistema",
                ex.getMessage() != null ? ex.getMessage() : "Error desconocido",
                HttpStatus.INTERNAL_SERVER_ERROR,
                getStackTrace(ex));
    }

    private ResponseEntity<?> buildError(String error, Object details, HttpStatus status, String technical) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", details);
        body.put("path", "");

        if (status == HttpStatus.INTERNAL_SERVER_ERROR) {
            body.put("technical_details", technical);
        }

        return new ResponseEntity<>(body, status);
    }

    private String extractDetailedMessage(String sqlMessage) {
        if (sqlMessage == null) {
            return "Error no especificado";
        }

        if (sqlMessage.contains("Duplicate entry")) {
            return "El registro ya existe. Verifica valores únicos como nombre o código";
        }
        if (sqlMessage.contains("foreign key")) {
            return "No se puede eliminar/modificar porque dependen otros registros";
        }
        if (sqlMessage.contains("Connection refused")) {
            return "No se pudo conectar a la base de datos. Verifica que MySQL esté activo";
        }
        if (sqlMessage.contains("Access denied")) {
            return "Acceso denegado a la base de datos. Verifica credenciales";
        }
        if (sqlMessage.contains("Column") && sqlMessage.contains("not found")) {
            return "Campo no encontrado en la tabla";
        }
        if (sqlMessage.contains("Table") && sqlMessage.contains("not found")) {
            return "Tabla no encontrada en la base de datos";
        }

        return sqlMessage;
    }

    private String getStackTrace(Exception ex) {
        StringBuilder sb = new StringBuilder();
        sb.append(ex.getClass().getName()).append(": ").append(ex.getMessage()).append("\n");

        StackTraceElement[] elements = ex.getStackTrace();
        for (int i = 0; i < Math.min(5, elements.length); i++) {
            sb.append("\tat ").append(elements[i]).append("\n");
        }

        return sb.toString();
    }
}