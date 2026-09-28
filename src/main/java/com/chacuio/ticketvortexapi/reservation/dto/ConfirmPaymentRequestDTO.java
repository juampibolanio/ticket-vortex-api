package com.chacuio.ticketvortexapi.reservation.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ConfirmPaymentRequestDTO(
        @NotNull(message = "Idempotency Key is required")
        UUID idempotencyKey,

        @NotNull(message = "Transaction Id is required")
        UUID transactionId
) { }
