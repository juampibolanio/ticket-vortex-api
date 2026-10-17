package com.chacuio.ticketvortexapi.zone.controller;

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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ZoneControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ZoneRepository zoneRepository;

    private Event testEvent;

    @BeforeEach
    void setUp() {
        zoneRepository.deleteAll();
        eventRepository.deleteAll();

        Event event = Event.builder()
                .title("Lollapalooza 2027")
                .description("Festival de música")
                .date(LocalDateTime.now().plusMonths(2))
                .location("Hipódromo de San Isidro")
                .build();
        testEvent = eventRepository.save(event);
    }

    @Test
    @DisplayName("Integration: Should return zone by ID successfully")
    @WithMockUser(roles = "USER")
    void findById_Success() throws Exception {
        Zone zone = Zone.builder()
                .name("Campo General")
                .description("Acceso general al predio")
                .price(new BigDecimal("12000.00"))
                .capacity(500)
                .event(testEvent)
                .build();
        Zone savedZone = zoneRepository.save(zone);

        mockMvc.perform(get("/api/v1/zones/{id}", savedZone.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedZone.getId().toString())))
                .andExpect(jsonPath("$.name", is("Campo General")))
                .andExpect(jsonPath("$.event.title", is("Lollapalooza 2027")));
    }

    @Test
    @DisplayName("Integration: Should return 404 when zone does not exist")
    @WithMockUser(roles = "USER")
    void findById_NotFound() throws Exception {
        UUID nonExistingId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/zones/{id}", nonExistingId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Integration: Should return all zones by event ID")
    @WithMockUser(roles = "USER")
    void findAllByEventId_Success() throws Exception {
        Zone zone1 = Zone.builder().name("VIP").price(new BigDecimal("30000")).capacity(100).event(testEvent).build();
        Zone zone2 = Zone.builder().name("Campo").price(new BigDecimal("15000")).capacity(300).event(testEvent).build();
        zoneRepository.save(zone1);
        zoneRepository.save(zone2);

        mockMvc.perform(get("/api/v1/zones/events/{eventId}", testEvent.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("Integration: Should allow ADMIN to create a new zone for an event")
    @WithMockUser(roles = "ADMIN")
    void createZone_AsAdmin_Success() throws Exception {
        ZoneRequestDTO requestDto = new ZoneRequestDTO(
                "Platea Preferencial",
                "Ubicación con asiento",
                new BigDecimal("25000.00"),
                250
        );

        mockMvc.perform(post("/api/v1/zones/events/{eventId}", testEvent.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Platea Preferencial")))
                .andExpect(jsonPath("$.capacity", is(250)));
    }

    @Test
    @DisplayName("Integration: Should return 403 Forbidden when a USER tries to create a zone")
    @WithMockUser(roles = "USER")
    void createZone_AsUser_Forbidden() throws Exception {
        ZoneRequestDTO requestDto = new ZoneRequestDTO(
                "Hack Zone",
                "Intento no autorizado",
                new BigDecimal("1000.00"),
                10
        );

        mockMvc.perform(post("/api/v1/zones/events/{eventId}", testEvent.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
    }
}
