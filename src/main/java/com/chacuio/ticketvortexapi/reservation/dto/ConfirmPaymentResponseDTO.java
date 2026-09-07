package com.chacuio.ticketvortexapi.reservation.dto;

import com.chacuio.ticketvortexapi.reservation.model.Status;

import java.time.Instant;
import java.util.UUID;

public record ConfirmPaymentResponseDTO(
        UUID reservationId,
        UUID idempotencyKey,
        UUID transactionId,
        Status status,
        Instant confirmedAt
) {}
