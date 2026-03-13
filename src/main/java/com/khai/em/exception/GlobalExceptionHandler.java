package com.khai.em.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import org.springframework.security.access.AccessDeniedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice 
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String errorMessage = error.getDefaultMessage();
            if (error instanceof FieldError fieldError) {
                errors.put(fieldError.getField(), errorMessage);
            } else {
                errors.put(error.getObjectName(), errorMessage);
            }
        });
        
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String path = violation.getPropertyPath() != null ? violation.getPropertyPath().toString() : "param";
            String key = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
            errors.put(key, violation.getMessage());
        }
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        Map<String, String> error = new HashMap<>();
        String name = ex.getName() != null ? ex.getName() : "parameter";
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "valid type";
        error.put(name, "must be a valid " + requiredType);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, String>> handleMissingRequestParam(MissingServletRequestParameterException ex) {
        Map<String, String> error = new HashMap<>();
        error.put(ex.getParameterName(), "Missing required parameter");
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleInvalidJson(HttpMessageNotReadableException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("message", "Invalid request body (malformed JSON or wrong Content-Type)");
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());
        // Thường dùng cho trường hợp chưa đăng nhập / không đủ trạng thái để xử lý.
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("message", "Access denied");
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnhandled(Exception ex) {
        log.error("Unhandled exception", ex);
        Map<String, String> error = new HashMap<>();
        error.put("message", "Unexpected server error");
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}