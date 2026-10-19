package com.chacuio.ticketvortexapi.event.service;

import com.chacuio.ticketvortexapi.event.dto.EventRequestDTO;
import com.chacuio.ticketvortexapi.event.dto.EventResponseDTO;
import com.chacuio.ticketvortexapi.event.dto.EventSummaryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface EventService {
    Page<EventSummaryDTO> findAll(Pageable pageable);
    EventResponseDTO findOneById(UUID id);
    EventResponseDTO create(EventRequestDTO eventRequestDTO);
}
