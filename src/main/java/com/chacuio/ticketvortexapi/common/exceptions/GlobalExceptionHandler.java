package com.chacuio.ticketvortexapi.common.exceptions;

import com.chacuio.ticketvortexapi.reservation.exception.NotEnoughCapacityException;
import com.chacuio.ticketvortexapi.reservation.exception.ReservationExpiredException;
import com.chacuio.ticketvortexapi.reservation.exception.TicketLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Clock;
import java.time.Instant;
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
        return response(ex.getMessage(), HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler({
            NotEnoughCapacityException.class,
            ReservationExpiredException.class,
            TicketLimitExceededException.class
    })
    public ResponseEntity<ErrorResponseDTO> handleReservationRequestException(
            RuntimeException ex,
            HttpServletRequest request) {
        return response(ex.getMessage(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        String message = ex.getBindingResult().getAllErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        if (message.isBlank()) {
            message = "Request validation failed";
        }

        return response(message, HttpStatus.BAD_REQUEST, request);
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
}
