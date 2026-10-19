package com.chacuio.ticketvortexapi.event.service;

import com.chacuio.ticketvortexapi.common.exceptions.ResourceNotFoundException;
import com.chacuio.ticketvortexapi.event.dto.EventRequestDTO;
import com.chacuio.ticketvortexapi.event.dto.EventResponseDTO;
import com.chacuio.ticketvortexapi.event.dto.EventSummaryDTO;
import com.chacuio.ticketvortexapi.event.mapper.EventMapper;
import com.chacuio.ticketvortexapi.event.model.Event;
import com.chacuio.ticketvortexapi.event.repository.EventRepository;
import com.chacuio.ticketvortexapi.zone.dto.ZoneSummaryDTO;
import com.chacuio.ticketvortexapi.zone.service.ZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final ZoneService zoneService;
    private final EventMapper eventMapper;

    @Override
    public Page<EventSummaryDTO> findAll(Pageable pageable) {
        return eventRepository.findAll(pageable)
                .map(eventMapper::toDtoSummary);
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

        List<ZoneSummaryDTO> createdZones = zoneService.createAll(savedEvent, dto.zones());

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
