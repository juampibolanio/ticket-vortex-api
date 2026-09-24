package com.chacuio.ticketvortexapi.event.service;

import com.chacuio.ticketvortexapi.common.exceptions.ResourceNotFoundException;
import com.chacuio.ticketvortexapi.event.dto.EventRequestDTO;
import com.chacuio.ticketvortexapi.event.dto.EventResponseDTO;
import com.chacuio.ticketvortexapi.event.dto.EventSummaryDTO;
import com.chacuio.ticketvortexapi.event.mapper.EventMapper;
import com.chacuio.ticketvortexapi.event.model.Event;
import com.chacuio.ticketvortexapi.event.repository.EventRepository;
import com.chacuio.ticketvortexapi.zone.dto.ZoneRequestDTO;
import com.chacuio.ticketvortexapi.zone.dto.ZoneSummaryDTO;
import com.chacuio.ticketvortexapi.zone.mapper.ZoneMapper;
import com.chacuio.ticketvortexapi.zone.service.ZoneService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final ZoneService zoneService;
    private final EventMapper eventMapper;

    @Override
    public List<EventSummaryDTO> findAll() {
        List<Event>  events = eventRepository.findAll();

        return events.stream()
                .map(eventMapper::toDtoSummary)
                .toList();
    }

    @Override
    public EventResponseDTO findOneById(UUID id) {
        return eventMapper.toDto(eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id)));
    }

    @Override
    @Transactional
    public EventResponseDTO create(EventRequestDTO dto) {
        Event event = Event.builder()
                .title(dto.title())
                .description(dto.description())
                .date(dto.date())
                .location(dto.location())
                .build();

        Event savedEvent = eventRepository.save(event);

        List<ZoneSummaryDTO> createdZones = new ArrayList<>();
        for (ZoneRequestDTO zoneDto : dto.zones()) {
            ZoneRequestDTO zoneWithEventId = new ZoneRequestDTO(
                    zoneDto.name(),
                    zoneDto.description(),
                    zoneDto.price(),
                    zoneDto.capacity(),
                    savedEvent.getId()
            );

            ZoneSummaryDTO createdZone = zoneService.create(zoneWithEventId);
            createdZones.add(createdZone);
        }

        return new EventResponseDTO(
                savedEvent.getId(),
                savedEvent.getTitle(),
                savedEvent.getDescription(),
                savedEvent.getDate(),
                savedEvent.getLocation(),
                createdZones,
                savedEvent.getCreatedAt(),
                savedEvent.getUpdatedAt()
        );
    }
}
