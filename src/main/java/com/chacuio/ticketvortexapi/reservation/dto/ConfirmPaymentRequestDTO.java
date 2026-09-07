package com.chacuio.ticketvortexapi.reservation.dto;

import java.util.UUID;

public record ConfirmPaymentRequestDTO(
        UUID reservationId,
        UUID idempotencyKey,
        UUID transactionId
) { }
