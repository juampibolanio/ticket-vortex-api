package com.chacuio.ticketvortexapi.common.exceptions;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request) {
        return response(ex.getMessage(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(DataConflictException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataConflictException(
            DataConflictException ex,
            HttpServletRequest request) {
        return response(ex.getMessage(), HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {
        log.warn("Data integrity conflict at {}", request.getRequestURI());
        return response("The request conflicts with existing data", HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {
        return response("Access denied: You do not have permission to access this resource", HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadRequestException(
            BadRequestException ex,
            HttpServletRequest request) {
        return response(ex.getMessage(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "valid type";
        String message = String.format("Parameter '%s' should be of type %s", ex.getName(), requiredType);
        return response(message, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {
        String message = String.format("HTTP method '%s' is not supported for this endpoint", ex.getMethod());
        return response(message, HttpStatus.METHOD_NOT_ALLOWED, request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNoResourceFound(
            NoResourceFoundException ex,
            HttpServletRequest request) {
        return response(ex.getMessage(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value",
                        (existingMessage, newMessage) -> existingMessage + "; " + newMessage
                ));

        return response(ex.getMessage(), HttpStatus.BAD_REQUEST, request, errors);
    }

    @ExceptionHandler({
            JwtException.class,
            AuthenticationException.class
    })
    public ResponseEntity<ErrorResponseDTO> handleAuthenticationException(
            Exception ex,
            HttpServletRequest request) {
        return response(ex.getMessage(), HttpStatus.UNAUTHORIZED, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleUnreadableRequest(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        return response("Request body is invalid", HttpStatus.BAD_REQUEST, request);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        log.error("An unexpected error occurred at {}", request.getRequestURI(), ex);
        return response("An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR, request);
    }


    private ResponseEntity<ErrorResponseDTO> response(
            String message,
            HttpStatus status,
            HttpServletRequest request) {
        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .message(message)
                .status(status.value())
                .path(request.getRequestURI())
                .timestamp(Instant.now(Clock.systemUTC()))
                .build();
        return ResponseEntity.status(status).body(error);
    }

    private ResponseEntity<ErrorResponseDTO> response(
            String message,
            HttpStatus status,
            HttpServletRequest request,
            Map<String, String> fieldErrors) {

        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .message(message)
                .status(status.value())
                .path(request.getRequestURI())
                .timestamp(Instant.now(Clock.systemUTC()))
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.status(status).body(error);
    }

}
