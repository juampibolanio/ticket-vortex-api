package com.chacuio.ticketvortexapi.zone.dto;

import java.util.UUID;

public record ZoneSummaryDTO(
        UUID id,
        String name,
        UUID eventId
) {}
