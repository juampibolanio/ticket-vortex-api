package com.chacuio.ticketvortexapi.event.dto;

import com.chacuio.ticketvortexapi.reservation.model.Status;
import com.chacuio.ticketvortexapi.user.dto.UserSummaryDTO;
import com.chacuio.ticketvortexapi.zone.dto.ZoneSummaryDTO;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record EventResponseDTO(
        UUID id,
        String title,
        String description,
        LocalDateTime date,
        String location,
        Instant createdAt,
        Instant updatedAt
) {}
