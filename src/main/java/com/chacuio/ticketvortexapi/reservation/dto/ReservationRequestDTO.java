package com.chacuio.ticketvortexapi.reservation.dto;

import java.util.UUID;

public record ReservationRequestDTO(
        UUID zoneId,
        Integer quantity,
        UUID idempotencyKey
) {}
