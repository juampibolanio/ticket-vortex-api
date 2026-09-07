package com.chacuio.ticketvortexapi.zone.dto;

import com.chacuio.ticketvortexapi.event.dto.EventSummaryDTO;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ZoneResponseDTO(
        UUID id,
        String name,
        String description,
        BigDecimal price,
        Integer capacity,
        EventSummaryDTO event,
        Integer version,
        Instant createdAt,
        Instant updatedAt
) {}
