package com.chacuio.ticketvortexapi.event.mapper;

import com.chacuio.ticketvortexapi.event.dto.EventResponseDTO;
import com.chacuio.ticketvortexapi.event.dto.EventSummaryDTO;
import com.chacuio.ticketvortexapi.event.model.Event;
import com.chacuio.ticketvortexapi.zone.mapper.ZoneMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventMapper {
    private final ZoneMapper zoneMapper;

    public EventResponseDTO toDto(Event event) {
        return new EventResponseDTO(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getDate(),
                event.getLocation(),
                event.getZones().stream()
                                .map(zoneMapper::toDtoSummary)
                                .toList(),
                event.getCreatedAt(),
                event.getUpdatedAt()
        );
    }

    public Event toEntity(EventResponseDTO dto) {
        return Event.builder()
                .title(dto.title())
                .description(dto.description())
                .date(dto.date())
                .location(dto.location())
                .build();
    }

    public EventSummaryDTO toDtoSummary(Event event) {
        if (event == null) return null;

        return new EventSummaryDTO(
                event.getId(),
                event.getTitle(),
                event.getDate(),
                event.getLocation()
        );
    }
}
