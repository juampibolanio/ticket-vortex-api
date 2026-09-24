package com.chacuio.ticketvortexapi.event.service;

import com.chacuio.ticketvortexapi.common.exceptions.ResourceNotFoundException;
import com.chacuio.ticketvortexapi.event.dto.EventResponseDTO;
import com.chacuio.ticketvortexapi.event.mapper.EventMapper;
import com.chacuio.ticketvortexapi.event.model.Event;
import com.chacuio.ticketvortexapi.event.repository.EventRepository;
import com.chacuio.ticketvortexapi.zone.service.ZoneService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
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
    @DisplayName("Should return a EventResponseDTO if the creation was successful")
    void createNewEvent_Success() {

    }
}
