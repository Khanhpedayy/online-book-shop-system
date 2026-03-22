package com.example.onlinebookshop.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ManagerGlobalExceptionHandler {

    /* â”€â”€ 400: Business validation errors â”€â”€ */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleJsonParseError(HttpMessageNotReadableException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Dữ liệu gửi lên không đúng định dạng. Có trường dữ liệu không hợp lệ hoặc không tồn tại trên hệ thống.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        StringBuilder errors = new StringBuilder();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.append(error.getField()).append(": ").append(error.getDefaultMessage()).append("; ");
        }
        return buildResponse(HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ: " + errors.toString());
    }

    /* â”€â”€ 404: Not found (RuntimeException with "not found" convention) â”€â”€ */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        ex.printStackTrace(); // FOR DEBUGGING: Print exception to backend console
        String msg = ex.getMessage();
        if (msg != null && msg.toLowerCase().contains("not found")) {
            return buildResponse(HttpStatus.NOT_FOUND, msg);
        }
        // Return exact message instead of generic to debug what's causing 400
        return buildResponse(HttpStatus.BAD_REQUEST, "System Error: " + msg);
    }

    /* â”€â”€ 500: Catch-all â”€â”€ */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(status).body(body);
    }
}

