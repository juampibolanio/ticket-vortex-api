package com.chacuio.ticketvortexapi.event.service;

import com.chacuio.ticketvortexapi.event.dto.EventRequestDTO;
import com.chacuio.ticketvortexapi.event.dto.EventResponseDTO;
import com.chacuio.ticketvortexapi.event.dto.EventSummaryDTO;

import java.util.List;
import java.util.UUID;

public interface EventService {
    List<EventSummaryDTO> findAll();
    EventResponseDTO findOneById(UUID id);
    EventResponseDTO create(EventRequestDTO eventRequestDTO);
}
