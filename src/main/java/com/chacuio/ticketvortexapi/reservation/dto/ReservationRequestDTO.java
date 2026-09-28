package com.chacuio.ticketvortexapi.reservation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record ReservationRequestDTO(
        @NotNull(message = "Zone Id is required")
        UUID zoneId,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be greater than zero")
        @Min(1)
        Integer quantity,

        @NotNull(message = "Idempotency Key is required")
        UUID idempotencyKey
) {}
