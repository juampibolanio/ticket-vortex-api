package com.chacuio.ticketvortexapi.event.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record EventSummaryDTO (
        UUID id,
        String title,
        LocalDateTime date,
        String location
){}
