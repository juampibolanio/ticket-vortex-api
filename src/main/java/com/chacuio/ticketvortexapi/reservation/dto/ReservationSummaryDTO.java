package com.chacuio.ticketvortexapi.reservation.dto;

import com.chacuio.ticketvortexapi.reservation.model.Status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ReservationSummaryDTO(
        UUID id,
        UUID customerId,
        String customerEmail,
        UUID zoneId,
        String zoneName,
        Status status,
        BigDecimal price,
        Instant expiresAt,
        Instant createdAt
) {}
