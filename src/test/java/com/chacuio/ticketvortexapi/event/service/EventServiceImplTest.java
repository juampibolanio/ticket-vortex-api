package com.chacuio.ticketvortexapi.event.service;

import com.chacuio.ticketvortexapi.common.exceptions.ResourceNotFoundException;
import com.chacuio.ticketvortexapi.event.dto.EventRequestDTO;
import com.chacuio.ticketvortexapi.event.dto.EventResponseDTO;
import com.chacuio.ticketvortexapi.event.mapper.EventMapper;
import com.chacuio.ticketvortexapi.event.model.Event;
import com.chacuio.ticketvortexapi.event.repository.EventRepository;
import com.chacuio.ticketvortexapi.zone.dto.ZoneRequestDTO;
import com.chacuio.ticketvortexapi.zone.dto.ZoneSummaryDTO;
import com.chacuio.ticketvortexapi.zone.service.ZoneService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {
    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventMapper eventMapper;

    @Mock
    private ZoneService zoneService;

    @InjectMocks
    private EventService eventService;

    @Test
    @DisplayName("Should return a EventResponseDTO when event exists ")
    void findOneById_Success() {
        // given
        UUID eventId = UUID.randomUUID();
        Event event = new Event();
        event.setId(eventId);

        EventResponseDTO expectedResponseDto = new EventResponseDTO(
                eventId,
                "John's band",
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventMapper.toDto(event)).thenReturn(expectedResponseDto);

        // when
        EventResponseDTO result = eventService.findOneById(eventId);

        // then / assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(eventId);
        verify(eventRepository, times(1)).findById(eventId);
        verify(eventMapper, times(1)).toDto(event);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when event does not exists")
    void findOneById_NotFound() {
        // given
        UUID eventId = UUID.randomUUID();
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> eventService.findOneById(eventId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Event not found with id: " + eventId);
    }

    @Test
    @DisplayName("Should return an EventResponseDTO if the creation was successful")
    void createNewEvent_Success() {
        // given / arrange
        UUID eventId = UUID.randomUUID();
        UUID zoneId = UUID.randomUUID();

        LocalDateTime eventDate = LocalDateTime.now().plusDays(10);
        Instant createdAt = Instant.now();
        Instant updatedAt = Instant.now();

        ZoneRequestDTO zoneRequest = new ZoneRequestDTO(
                "VIP",
                "VIP zone",
                new BigDecimal("10000"),
                100,
                null
        );

        EventRequestDTO eventRequest = new EventRequestDTO(
                "Rock Festival",
                "Rock festival description",
                eventDate,
                "Resistencia",
                List.of(zoneRequest)
        );

        Event savedEvent = Event.builder()
                .id(eventId)
                .title(eventRequest.title())
                .description(eventRequest.description())
                .date(eventRequest.date())
                .location(eventRequest.location())
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        ZoneSummaryDTO createdZone = new ZoneSummaryDTO(
                zoneId,
                "VIP",
                100,
                eventId
        );

        when(eventRepository.save(any(Event.class)))
                .thenReturn(savedEvent);

        when(zoneService.create(any(ZoneRequestDTO.class)))
                .thenReturn(createdZone);

        ArgumentCaptor<Event> eventCaptor =
                ArgumentCaptor.forClass(Event.class);

        ArgumentCaptor<ZoneRequestDTO> zoneCaptor =
                ArgumentCaptor.forClass(ZoneRequestDTO.class);

        // when / act
        EventResponseDTO result = eventService.create(eventRequest);

        // then / assert
        assertNotNull(result);

        assertEquals(eventId, result.id());
        assertEquals(eventRequest.title(), result.title());
        assertEquals(eventRequest.description(), result.description());
        assertEquals(eventRequest.date(), result.date());
        assertEquals(eventRequest.location(), result.location());

        assertEquals(1, result.zones().size());
        assertEquals(createdZone, result.zones().getFirst());

        assertEquals(createdAt, result.createdAt());
        assertEquals(updatedAt, result.updatedAt());

        // Verify eventRepository.save()

        verify(eventRepository).save(eventCaptor.capture());

        Event eventSentToRepository = eventCaptor.getValue();

        assertEquals(eventRequest.title(), eventSentToRepository.getTitle());
        assertEquals(eventRequest.description(), eventSentToRepository.getDescription());
        assertEquals(eventRequest.date(), eventSentToRepository.getDate());
        assertEquals(eventRequest.location(), eventSentToRepository.getLocation());

        // Verify zoneService.create()

        verify(zoneService).create(zoneCaptor.capture());

        ZoneRequestDTO zoneSentToService = zoneCaptor.getValue();

        assertEquals(zoneRequest.name(), zoneSentToService.name());
        assertEquals(zoneRequest.description(), zoneSentToService.description());
        assertEquals(zoneRequest.price(), zoneSentToService.price());
        assertEquals(zoneRequest.capacity(), zoneSentToService.capacity());

        // The zone must receive the ID generated by the event

        assertEquals(eventId, zoneSentToService.eventId());
    }
}
