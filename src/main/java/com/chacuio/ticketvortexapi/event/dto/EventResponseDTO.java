package com.chacuio.ticketvortexapi.event.dto;

import com.chacuio.ticketvortexapi.reservation.model.Status;
import com.chacuio.ticketvortexapi.user.dto.UserSummaryDTO;
import com.chacuio.ticketvortexapi.zone.dto.ZoneSummaryDTO;

import java.time.Instant;
import java.util.UUID;

public record EventResponseDTO(
        UUID id,
        Status status,
        UUID idempotencyKey,
        UserSummaryDTO customer,
        ZoneSummaryDTO zone,
        Instant expiresAt,
        Instant createdAt,
        Instant updatedAt
) {}
