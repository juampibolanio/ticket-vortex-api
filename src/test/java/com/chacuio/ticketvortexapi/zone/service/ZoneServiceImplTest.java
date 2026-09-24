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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ZoneServiceImplTest {
    @Mock
    private ZoneRepository zoneRepository;

    @Mock
    private ZoneMapper zoneMapper;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private ZoneServiceImpl zoneService;

    @Test
    @DisplayName("Should return ZoneResponseDTO when zone exists")
    void findByIdWithEvent_Success() {
        // given (preparation)
        UUID zoneId = UUID.randomUUID();
        Zone zone = new Zone();
        zone.setId(zoneId);

        ZoneResponseDTO expectedResponse = new ZoneResponseDTO(
                zoneId, "VIP", null, null, 100, null, 1, null, null
        );

        when(zoneRepository.findByIdWithEvent(zoneId)).thenReturn(Optional.of(zone));
        when(zoneMapper.toDto(zone)).thenReturn(expectedResponse);

        // when (execution)
        ZoneResponseDTO result = zoneService.findByIdWithEvent(zoneId);

        // then (asserts)
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(zoneId);
        verify(zoneRepository, times(1)).findByIdWithEvent(zoneId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when zone does not exist")
    void findByIdWithEvent_NotFound() {
        // given
        UUID zoneId = UUID.randomUUID();
        when(zoneRepository.findByIdWithEvent(zoneId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> zoneService.findByIdWithEvent(zoneId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Zone not found with id: " + zoneId);
    }

    @Test
    @DisplayName("Should return a ZoneSummaryDTO if the creation was successful")
    void createNewZone_Success() {
        // given
        UUID eventId = UUID.randomUUID();
        Event event = new Event();
        event.setId(eventId);

        UUID zoneId = UUID.randomUUID();
        ZoneRequestDTO requestDto = new ZoneRequestDTO("VIP", null, new BigDecimal(2000), 20, eventId);

        Zone zoneEntity = new Zone();
        Zone savedZone = new Zone();
        savedZone.setId(zoneId);

        ZoneSummaryDTO expectedDto = new ZoneSummaryDTO(zoneId, "VIP", 20, eventId);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(zoneMapper.toEntity(requestDto, event)).thenReturn(zoneEntity);
        when(zoneRepository.save(zoneEntity)).thenReturn(savedZone);
        when(zoneMapper.toDtoSummary(savedZone)).thenReturn(expectedDto);

        // when
        ZoneSummaryDTO result = zoneService.create(requestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(zoneId);

        verify(eventRepository, times(1)).findById(eventId);
        verify(zoneRepository, times(1)).save(zoneEntity);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when event does not exist during zone creation")
    void createNewZone_EventNotFound() {
        // given
        UUID eventId = UUID.randomUUID();
        ZoneRequestDTO requestDto = new ZoneRequestDTO("VIP", null, new BigDecimal(2000), 20, eventId);

        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> zoneService.create(requestDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Event not found with id: " + eventId);

        verifyNoInteractions(zoneRepository.save(any()));
    }
}
