package io.plan.mate.expense.tracker.backend.commons.exception.handling;

import io.plan.mate.expense.tracker.backend.commons.exception.handling.dto.ApiError;
import io.plan.mate.expense.tracker.backend.commons.exception.handling.exception.BadRequestException;
import io.plan.mate.expense.tracker.backend.commons.exception.handling.exception.ConflictException;
import io.plan.mate.expense.tracker.backend.commons.exception.handling.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiError> handleResourceNotFound(
      final ResourceNotFoundException ex, final HttpServletRequest request) {

    final ApiError error =
        ApiError.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error("Not found")
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .build();

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ApiError> handleBadRequest(
      final BadRequestException ex, final HttpServletRequest request) {

    final ApiError error =
        ApiError.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Bad Request")
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleMethodArgumentNotValid(
      final MethodArgumentNotValidException ex, final HttpServletRequest request) {

    log.warn(
        "Validation failed for request {} {}: {}",
        request.getMethod(),
        request.getRequestURI(),
        ex.getMessage());

    final String message =
        ex.getBindingResult().getFieldErrors().stream()
            .map(err -> err.getField() + ": " + err.getDefaultMessage())
            .collect(Collectors.joining("; "));

    final ApiError error =
        ApiError.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Error")
            .message(message.isEmpty() ? "Validation error" : message)
            .path(request.getRequestURI())
            .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ApiError> handleConflict(
      final ConflictException ex, final HttpServletRequest request) {

    final ApiError error =
        ApiError.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.CONFLICT.value())
            .error("Conflict")
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .build();

    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiError> handleAccessDenied(final HttpServletRequest request) {

    final ApiError error =
        ApiError.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.FORBIDDEN.value())
            .error("Forbidden")
            .message("You do not have permission to perform this action")
            .path(request.getRequestURI())
            .build();

    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiError> handleDataIntegrityViolation(
      final DataIntegrityViolationException ex, final HttpServletRequest request) {

    log.warn(
        "Data integrity violation for request {} {}: {}",
        request.getMethod(),
        request.getRequestURI(),
        ex.getMessage());

    final ApiError error =
        ApiError.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.CONFLICT.value())
            .error("Conflict")
            .message("Request conflicts with existing data")
            .path(request.getRequestURI())
            .build();

    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleGeneric(
      final Exception ex, final HttpServletRequest request) {

    log.error(
        "Unhandled exception for request {} {}", request.getMethod(), request.getRequestURI(), ex);

    final ApiError error =
        ApiError.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("Internal Server Error")
            .message("An unexpected error occurred")
            .path(request.getRequestURI())
            .build();

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }
}
