package com.example.onlinebookshop.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    /* â”€â”€ 404: Not found (RuntimeException with "not found" convention) â”€â”€ */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        String msg = ex.getMessage();
        if (msg != null && msg.toLowerCase().contains("not found")) {
            return buildResponse(HttpStatus.NOT_FOUND, msg);
        }
        // Other runtime errors â†’ 400 to avoid leaking stack traces
        return buildResponse(HttpStatus.BAD_REQUEST, msg != null ? msg : "An unexpected error occurred");
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

