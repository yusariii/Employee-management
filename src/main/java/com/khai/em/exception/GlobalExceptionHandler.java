package com.khai.em.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.khai.em.dto.error.response.ErrorResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

@Slf4j
@RestControllerAdvice 
public class GlobalExceptionHandler {


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .findFirst()
        .orElse("Validation failed");

        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message, request.getRequestURI());

        log.warn("Validation failed: {} {} - {}", request.getMethod(), request.getRequestURI(), message);


        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        String message = ex.getConstraintViolations().stream()
                .findFirst()
                .map(violation -> {
                    String path = violation.getPropertyPath() != null ? violation.getPropertyPath().toString() : "param";
                    String key = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
                    return key + ": " + violation.getMessage();
                })
                .orElse("Validation failed");

        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message, request.getRequestURI());

        log.warn("Constraint violation: {} {} - {}", request.getMethod(), request.getRequestURI(), message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String name = ex.getName() != null ? ex.getName() : "parameter";
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "valid type";
        String message = name + ": must be a valid " + requiredType;

        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message, request.getRequestURI());

        log.warn("Type mismatch: {} {} - {}", request.getMethod(), request.getRequestURI(), message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestParam(MissingServletRequestParameterException ex, HttpServletRequest request) {
        String message = ex.getParameterName() + ": Missing required parameter";

        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message, request.getRequestURI());

        log.warn("Missing request parameter: {} {} - {}", request.getMethod(), request.getRequestURI(), message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidJson(HttpMessageNotReadableException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid request body (malformed JSON or wrong Content-Type)",
                request.getRequestURI());

        log.warn("Invalid request body: {} {} - {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), request.getRequestURI());

        log.warn("Illegal argument: {} {} - {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), ex.getMessage(), request.getRequestURI());

        log.warn("Illegal state: {} {} - {}", request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(HttpStatus.FORBIDDEN.value(), "Access denied", request.getRequestURI());

        log.warn("Access denied: {} {}", request.getMethod(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Invalid username or password",
                request.getRequestURI());

        log.warn("Authentication failed: {} {} - {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnhandled(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception: {} {}", request.getMethod(), request.getRequestURI(), ex);
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Unexpected server error",
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}