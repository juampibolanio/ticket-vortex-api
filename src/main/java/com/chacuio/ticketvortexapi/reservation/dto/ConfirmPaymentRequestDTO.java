package com.chacuio.ticketvortexapi.reservation.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record ConfirmPaymentRequestDTO(
        @NotBlank(message = "Transaction Id is required")
        UUID idempotencyKey,

        @NotBlank(message = "Transaction Id is required")
        UUID transactionId
) { }
