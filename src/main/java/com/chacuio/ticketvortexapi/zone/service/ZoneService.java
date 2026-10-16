package com.chacuio.ticketvortexapi.zone.service;

import com.chacuio.ticketvortexapi.event.model.Event;
import com.chacuio.ticketvortexapi.zone.dto.ZoneRequestDTO;
import com.chacuio.ticketvortexapi.zone.dto.ZoneResponseDTO;
import com.chacuio.ticketvortexapi.zone.dto.ZoneSummaryDTO;
import com.chacuio.ticketvortexapi.zone.model.Zone;

import java.util.List;
import java.util.UUID;

public interface ZoneService {
    List<ZoneSummaryDTO> findAllByEventId(UUID eventId);
    ZoneResponseDTO findByIdWithEvent(UUID id);
    Zone findEntityByIdWithLock(UUID id);
    List<ZoneSummaryDTO> createAll(Event event, List<ZoneRequestDTO> zoneDtos);
    ZoneSummaryDTO create(UUID eventId, ZoneRequestDTO zoneRequestDTO);
}
