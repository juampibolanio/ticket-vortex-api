package com.chacuio.ticketvortexapi.zone.service;

import com.chacuio.ticketvortexapi.common.exceptions.ResourceNotFoundException;
import com.chacuio.ticketvortexapi.event.model.Event;
import com.chacuio.ticketvortexapi.event.repository.EventRepository;
import com.chacuio.ticketvortexapi.zone.dto.ZoneRequestDTO;
import com.chacuio.ticketvortexapi.zone.dto.ZoneResponseDTO;
import com.chacuio.ticketvortexapi.zone.dto.ZoneSummaryDTO;
import com.chacuio.ticketvortexapi.zone.mapper.ZoneMapper;
import com.chacuio.ticketvortexapi.zone.model.Zone;
import com.chacuio.ticketvortexapi.zone.repository.ZoneRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ZoneServiceImpl implements ZoneService {
    private final ZoneRepository zoneRepository;
    private final EventRepository eventRepository;
    private final ZoneMapper mapper;

    @Override
    public List<ZoneSummaryDTO> findAllByEventId(UUID eventId) {
        return zoneRepository.findAllByEventId(eventId);
    }

    @Override
    public ZoneResponseDTO findByIdWithEvent(UUID id) {
        Zone zone = zoneRepository.findByIdWithEvent(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with id: " + id));
        return mapper.toDto(zone);
    }

    @Transactional
    @Override
    public ZoneSummaryDTO create(ZoneRequestDTO dto) {
        Event event = eventRepository.findById(dto.eventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + dto.eventId()));

        Zone zone = mapper.toEntity(dto, event);
        Zone savedZone = zoneRepository.save(zone);
        return mapper.toDtoSummary(savedZone);
    }
}
