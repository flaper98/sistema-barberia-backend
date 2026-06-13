package com.barberia.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest req) {
        log.warn("Recurso no encontrado: {} | path={}", ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildError(HttpStatus.NOT_FOUND, "No encontrado", ex.getMessage(), req));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(
            BusinessException ex, HttpServletRequest req) {
        log.warn("Regla de negocio: {} | path={}", ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildError(HttpStatus.BAD_REQUEST, "Error de negocio", ex.getMessage(), req));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
            ConflictException ex, HttpServletRequest req) {
        log.warn("Conflicto: {} | path={}", ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildError(HttpStatus.CONFLICT, "Conflicto", ex.getMessage(), req));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    if (error instanceof FieldError fe) {
                        return ErrorResponse.FieldError.builder()
                                .field(fe.getField())
                                .message(fe.getDefaultMessage())
                                .rejectedValue(fe.getRejectedValue())
                                .build();
                    }
                    return ErrorResponse.FieldError.builder()
                            .field(error.getObjectName())
                            .message(error.getDefaultMessage())
                            .build();
                })
                .toList();

        log.warn("Error de validación en {} campos | path={}", fieldErrors.size(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error("Datos inválidos")
                        .message("Uno o más campos tienen errores de validación")
                        .path(req.getRequestURI())
                        .fieldErrors(fieldErrors)
                        .build());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest req) {
        log.warn("Credenciales inválidas | path={}", req.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(buildError(HttpStatus.UNAUTHORIZED, "No autorizado",
                        "Credenciales inválidas", req));
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabled(
            DisabledException ex, HttpServletRequest req) {
        log.warn("Usuario deshabilitado | path={}", req.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(buildError(HttpStatus.UNAUTHORIZED, "No autorizado",
                        "Usuario deshabilitado", req));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuth(
            AuthenticationException ex, HttpServletRequest req) {
        log.warn("Error de autenticación: {} | path={}", ex.getClass().getSimpleName(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(buildError(HttpStatus.UNAUTHORIZED, "No autorizado",
                        "Acceso no autorizado", req));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest req) {
        log.warn("Acceso denegado | path={}", req.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(buildError(HttpStatus.FORBIDDEN, "Prohibido",
                        "No tiene permisos para realizar esta acción", req));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception ex, HttpServletRequest req) {
        log.error("Error inesperado | path={} | error={}", req.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno",
                        "Ocurrió un error inesperado. Contacte al administrador.", req));
    }

    private ErrorResponse buildError(HttpStatus status, String error, String message,
                                     HttpServletRequest req) {
        return ErrorResponse.builder()
                .status(status.value())
                .error(error)
                .message(message)
                .path(req.getRequestURI())
                .build();
    }
}
