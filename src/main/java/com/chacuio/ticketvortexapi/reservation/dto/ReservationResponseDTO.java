package com.chacuio.ticketvortexapi.reservation.dto;

import com.chacuio.ticketvortexapi.reservation.model.Status;
import com.chacuio.ticketvortexapi.user.dto.UserSummaryDTO;
import com.chacuio.ticketvortexapi.zone.dto.ZoneSummaryDTO;

import java.time.Instant;
import java.util.UUID;

public record ReservationResponseDTO(
        UUID id,
        UserSummaryDTO customer,
        ZoneSummaryDTO zone,
        Status status,
        Instant expiresAt,
        UUID idempotencyKey,
        Instant createdAt,
        Instant updatedAt
) { }
