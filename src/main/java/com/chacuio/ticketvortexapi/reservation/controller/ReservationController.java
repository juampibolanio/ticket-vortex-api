package com.chacuio.ticketvortexapi.reservation.controller;

import com.chacuio.ticketvortexapi.reservation.dto.ReservationRequestDTO;
import com.chacuio.ticketvortexapi.reservation.dto.ReservationResponseDTO;
import com.chacuio.ticketvortexapi.reservation.dto.ReservationSummaryDTO;
import com.chacuio.ticketvortexapi.reservation.dto.ConfirmPaymentRequestDTO;
import com.chacuio.ticketvortexapi.reservation.dto.ConfirmPaymentResponseDTO;
import com.chacuio.ticketvortexapi.reservation.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservations", description = "Endpoints for managing reservations")
public class ReservationController {
    private final ReservationService reservationService;

    @Operation(summary = "Get reservations by event ID", description = "Returns all reservations associated with an event.")
    @ApiResponse(responseCode = "200", description = "Reservations retrieved successfully")
    @GetMapping("/{eventId}")
    public ResponseEntity<List<ReservationSummaryDTO>> findAll(
            @Parameter(description = "Unique identifier of the event", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID eventId) {
        return ResponseEntity.ok(reservationService.findAllByEventId(eventId));
    }

    @Operation(summary = "Get reservations by customer ID", description = "Returns all reservations associated with a customer.")
    @ApiResponse(responseCode = "200", description = "Reservations retrieved successfully")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<ReservationSummaryDTO>> findAllByCustomerId(
            @Parameter(description = "Unique identifier of the customer", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID customerId) {
        return ResponseEntity.ok(reservationService.findAllByCustomerId(customerId));
    }

    @Operation(summary = "Get reservation by ID", description = "Returns the complete information of a reservation.")
    @ApiResponse(responseCode = "200", description = "Reservation found successfully")
    @ApiResponse(responseCode = "404", description = "Reservation not found")
    @GetMapping("/search/{reservationId}")
    public ResponseEntity<ReservationResponseDTO> findById(
            @Parameter(description = "Unique identifier of the reservation", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID reservationId) {
        return ResponseEntity.ok(reservationService.findById(reservationId));
    }

    @Operation(summary = "Create reservations", description = "Creates reservations for a customer using the supplied request data.")
    @ApiResponse(responseCode = "201", description = "Reservations created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid reservation data or insufficient capacity")
    @ApiResponse(responseCode = "404", description = "Customer or zone not found")
    @PostMapping("/{customerId}")
    public ResponseEntity<List<ReservationResponseDTO>> reserve(
            @Valid @RequestBody ReservationRequestDTO dto,
            @Parameter(description = "Unique identifier of the customer", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID customerId
    ) {
        List<ReservationResponseDTO> reservations = reservationService.reserve(dto, customerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservations);
    }

    @Operation(summary = "Confirm reservation payment", description = "Confirms all reservations associated with the idempotency key.")
    @ApiResponse(responseCode = "200", description = "Payment confirmed successfully")
    @ApiResponse(responseCode = "400", description = "Invalid payment confirmation data or reservation expired")
    @ApiResponse(responseCode = "404", description = "Reservations not found")
    @PostMapping("/confirm-payment")
    public ResponseEntity<ConfirmPaymentResponseDTO> confirmPayment(
            @Valid @RequestBody ConfirmPaymentRequestDTO dto
    ) {
        return ResponseEntity.ok(reservationService.confirmPayment(dto));
    }

    @Operation(summary = "Delete reservation", description = "Deletes a reservation by its ID.")
    @ApiResponse(responseCode = "204", description = "Reservation deleted successfully")
    @ApiResponse(responseCode = "404", description = "Reservation not found")
    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Unique identifier of the reservation", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID reservationId
    ) {
        reservationService.delete(reservationId);
        return ResponseEntity.noContent().build();
    }
}
