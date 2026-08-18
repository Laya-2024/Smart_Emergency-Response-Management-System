package com.smartresponse.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException ex) {
    FieldError error = ex.getBindingResult().getFieldError();
    String detail = error == null ? "Invalid request content." : error.getField() + ": " + error.getDefaultMessage();
    return problem(HttpStatus.BAD_REQUEST, detail);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  ResponseEntity<Map<String, Object>> invalidArgument(IllegalArgumentException ex) {
    return problem(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  @ExceptionHandler(NoSuchElementException.class)
  ResponseEntity<Map<String, Object>> missing(NoSuchElementException ex) {
    return problem(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(IllegalStateException.class)
  ResponseEntity<Map<String, Object>> conflict(IllegalStateException ex) {
    return problem(HttpStatus.CONFLICT, ex.getMessage());
  }

  @ExceptionHandler(SecurityException.class)
  ResponseEntity<Map<String, Object>> forbidden(SecurityException ex) {
    return problem(HttpStatus.FORBIDDEN, ex.getMessage());
  }

  private ResponseEntity<Map<String, Object>> problem(HttpStatus status, String detail) {
    return ResponseEntity.status(status).body(Map.of(
        "timestamp", Instant.now().toString(), "status", status.value(), "detail", detail));
  }
}
