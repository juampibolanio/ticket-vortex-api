package com.chacuio.ticketvortexapi.zone.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ZoneRequestDTO(
        String name,
        String description,
        BigDecimal price,
        Integer capacity,
        UUID eventId
) { }
