package com.chacuio.ticketvortexapi.event.dto;

import com.chacuio.ticketvortexapi.zone.dto.ZoneResponseDTO;
import com.chacuio.ticketvortexapi.zone.dto.ZoneSummaryDTO;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record EventResponseDTO(
        UUID id,
        String title,
        String description,
        LocalDateTime date,
        String location,
        List<ZoneSummaryDTO> zones,
        Instant createdAt,
        Instant updatedAt
) {}
