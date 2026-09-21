package com.chacuio.ticketvortexapi.zone.dto;

import com.chacuio.ticketvortexapi.event.dto.EventSummaryDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class ZoneResponseDTO {
    UUID id;
    String name;
    String description;
    BigDecimal price;
    Integer capacity;
    EventSummaryDTO event;
    Integer version;
    Instant createdAt;
    Instant updatedAt;
}
