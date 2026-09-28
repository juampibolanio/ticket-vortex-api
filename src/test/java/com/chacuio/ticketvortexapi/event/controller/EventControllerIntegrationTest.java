package com.chacuio.ticketvortexapi.event.controller;

import com.chacuio.ticketvortexapi.event.dto.EventRequestDTO;
import com.chacuio.ticketvortexapi.event.model.Event;
import com.chacuio.ticketvortexapi.event.repository.EventRepository;
import com.chacuio.ticketvortexapi.zone.dto.ZoneRequestDTO;
import com.chacuio.ticketvortexapi.zone.model.Zone;
import com.chacuio.ticketvortexapi.zone.repository.ZoneRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EventControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ZoneRepository zoneRepository;

    @BeforeEach
    void setUp() {
        zoneRepository.deleteAll();
        eventRepository.deleteAll();
    }

    @Test
    @DisplayName("Integration: Should create an event with its zones successfully")
    void createEventWithZones_Success() throws Exception {
        // Given / Arrange
        ZoneRequestDTO vipZone = new ZoneRequestDTO(
                "VIP",
                "VIP Zone Description",
                new BigDecimal("15000.00"),
                100,
                null
        );

        ZoneRequestDTO generalZone = new ZoneRequestDTO(
                "GENERAL",
                "General Admission",
                new BigDecimal("8000.00"),
                200,
                null
        );

        EventRequestDTO requestDto = new EventRequestDTO(
                "Cosquin Rock 2027",
                "Festival anual de Rock",
                LocalDateTime.now().plusMonths(5),
                "Cordoba",
                List.of(vipZone, generalZone)
        );

        // When & Then
        mockMvc.perform(post("/api/v1/events")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("Cosquin Rock 2027"))
                .andExpect(jsonPath("$.location").value("Cordoba"))
                .andExpect(jsonPath("$.zones", hasSize(2)))
                .andExpect(jsonPath("$.zones[0].name").value("VIP"))
                .andExpect(jsonPath("$.zones[1].name").value("GENERAL"));

        // Assertion in DB
        List<Event> eventsInDb = eventRepository.findAll();
        assertThat(eventsInDb).hasSize(1);

        List<Zone> zonesInDb = zoneRepository.findAll();
        assertThat(zonesInDb).hasSize(2);
        assertThat(zonesInDb.getFirst().getEvent().getId()).isEqualTo(eventsInDb.getFirst().getId());
    }

    @Test
    @DisplayName("Integration: Should return 404 when event id does not exist")
    void findById_NotFound() throws Exception {
        UUID nonExistingId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/events/{id}", nonExistingId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Integration: Should return 400 Bad Request when title is blank")
    void createEvent_InvalidTitle_BadRequest() throws Exception {
        ZoneRequestDTO zone = new ZoneRequestDTO(
                "VIP",
                "VIP Zone",
                new BigDecimal("15000.00"),
                100,
                null
        );

        EventRequestDTO invalidRequest = new EventRequestDTO(
                "",
                "Description",
                LocalDateTime.now().plusDays(5),
                "Location",
                List.of(zone)
        );

        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
