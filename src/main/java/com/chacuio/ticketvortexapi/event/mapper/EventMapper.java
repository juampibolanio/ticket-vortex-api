package com.chacuio.ticketvortexapi.event.mapper;

import com.chacuio.ticketvortexapi.event.dto.EventResponseDTO;
import com.chacuio.ticketvortexapi.event.dto.EventSummaryDTO;
import com.chacuio.ticketvortexapi.event.model.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {
    public EventResponseDTO toDto(Event event) {
        return new EventResponseDTO(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getDate(),
                event.getLocation(),
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
