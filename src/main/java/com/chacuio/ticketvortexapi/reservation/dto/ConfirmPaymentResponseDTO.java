package com.chacuio.ticketvortexapi.reservation.dto;

import com.chacuio.ticketvortexapi.reservation.model.Status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ConfirmPaymentResponseDTO(
        UUID idempotencyKey,
        UUID transactionId,
        Status status,
        Instant confirmedAt,
        List<ReservationSummaryDTO> reservations
) {}
