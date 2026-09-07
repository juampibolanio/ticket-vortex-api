package com.chacuio.ticketvortexapi.event.dto;

import java.time.LocalDateTime;

public record EventRequestDTO(
        String title,
        String description,
        LocalDateTime date,
        String location
) { }
