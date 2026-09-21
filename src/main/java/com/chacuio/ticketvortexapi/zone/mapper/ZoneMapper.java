package com.chacuio.ticketvortexapi.zone.mapper;

import com.chacuio.ticketvortexapi.event.mapper.EventMapper;
import com.chacuio.ticketvortexapi.event.model.Event;
import com.chacuio.ticketvortexapi.zone.dto.ZoneRequestDTO;
import com.chacuio.ticketvortexapi.zone.dto.ZoneResponseDTO;
import com.chacuio.ticketvortexapi.zone.dto.ZoneSummaryDTO;
import com.chacuio.ticketvortexapi.zone.model.Zone;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ZoneMapper {
    private final EventMapper eventMapper;

    public ZoneResponseDTO toDto(Zone zone) {
        return new ZoneResponseDTO(
                zone.getId(),
                zone.getName(),
                zone.getDescription(),
                zone.getPrice(),
                zone.getCapacity(),
                eventMapper.toDtoSummary(zone.getEvent()),
                zone.getVersion(),
                zone.getCreatedAt(),
                zone.getUpdatedAt()
        );
    }

    public Zone toEntity(ZoneRequestDTO dto, Event event) {
        return Zone.builder()
                .name(dto.name())
                .description(dto.description())
                .price(dto.price())
                .capacity(dto.capacity())
                .event(event)
                .build();
    }

    public ZoneSummaryDTO toDtoSummary(Zone zone) {
        if (zone == null) return null;

        return new ZoneSummaryDTO(
                zone.getId(),
                zone.getName(),
                zone.getCapacity(),
                zone.getEvent().getId()
        );
    }
}
