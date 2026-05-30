package com.bancodigital.backend.shared.api;

import com.bancodigital.backend.shared.exception.BusinessException;
import com.bancodigital.backend.shared.exception.ForbiddenException;
import com.bancodigital.backend.shared.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusiness(BusinessException ex, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiError> handleForbidden(ForbiddenException ex, HttpServletRequest request) {
        return buildError(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining(", "));
        return buildError(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        String detail = ex.getMostSpecificCause().getMessage();
        String message = "Cuerpo JSON invalido o tipos incorrectos. Detalle: " + detail;
        return buildError(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex,
            HttpServletRequest request) {
        String root = ex.getMostSpecificCause().getMessage();
        String message = "Violacion de reglas de datos en base de datos (unicidad, FK, CHECK, etc.). " + root;
        return buildError(HttpStatus.CONFLICT, message, request);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiError> handleDataAccess(DataAccessException ex, HttpServletRequest request) {
        String root = ex.getMostSpecificCause().getMessage();
        String message = "Error al acceder a la base de datos. " + root;
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, message, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest request) {
        Throwable cause = ex.getCause();
        String causeMsg = cause != null ? cause.getMessage() : null;
        String message = ex.getClass().getSimpleName() + ": "
                + (ex.getMessage() != null ? ex.getMessage() : "sin mensaje");
        if (causeMsg != null && !causeMsg.equals(ex.getMessage())) {
            message = message + " | Causa: " + causeMsg;
        }
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, message, request);
    }

    private String formatFieldError(FieldError err) {
        return err.getField() + ": " + err.getDefaultMessage();
    }

    private ResponseEntity<ApiError> buildError(HttpStatus status, String message, HttpServletRequest request) {
        ApiError body = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
