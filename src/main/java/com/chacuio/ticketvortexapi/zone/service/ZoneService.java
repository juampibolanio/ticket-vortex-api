package com.chacuio.ticketvortexapi.zone.service;

import com.chacuio.ticketvortexapi.zone.dto.ZoneRequestDTO;
import com.chacuio.ticketvortexapi.zone.dto.ZoneResponseDTO;
import com.chacuio.ticketvortexapi.zone.dto.ZoneSummaryDTO;

import java.util.List;
import java.util.UUID;

public interface ZoneService {
    List<ZoneSummaryDTO> findAllByEventId(UUID eventId);
    ZoneResponseDTO findByIdWithEvent(UUID id);
    ZoneSummaryDTO create(ZoneRequestDTO zoneRequestDTO);
}
