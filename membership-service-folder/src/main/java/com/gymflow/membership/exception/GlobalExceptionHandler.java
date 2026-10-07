package com.gymflow.membership.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        
        Map<String, Object> error = new HashMap<>();
        error.put("code", "RUNTIME_ERROR");
        error.put("message", ex.getMessage());
        error.put("details", new HashMap<>());
        
        body.put("error", error);
        body.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        
        Map<String, Object> error = new HashMap<>();
        error.put("code", "INTERNAL_ERROR");
        error.put("message", "An internal error occurred");
        error.put("details", new HashMap<>());
        
        body.put("error", error);
        body.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
