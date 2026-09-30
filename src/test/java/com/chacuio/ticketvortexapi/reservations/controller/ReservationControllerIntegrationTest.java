package com.chacuio.ticketvortexapi.reservations.controller;

import com.chacuio.ticketvortexapi.event.model.Event;
import com.chacuio.ticketvortexapi.event.repository.EventRepository;
import com.chacuio.ticketvortexapi.reservation.dto.ConfirmPaymentRequestDTO;
import com.chacuio.ticketvortexapi.reservation.dto.ReservationRequestDTO;
import com.chacuio.ticketvortexapi.reservation.model.Reservation;
import com.chacuio.ticketvortexapi.reservation.model.Status;
import com.chacuio.ticketvortexapi.reservation.repository.ReservationRepository;
import com.chacuio.ticketvortexapi.user.model.Role;
import com.chacuio.ticketvortexapi.user.model.User;
import com.chacuio.ticketvortexapi.user.repository.UserRepository;
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
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ReservationControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ZoneRepository zoneRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAll();
        zoneRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Integration: Should reserve and persist the requested tickets")
    void reserve_CreatesReservations() throws Exception {
        User customer = createCustomer();
        Zone zone = createZone(10);
        UUID idempotencyKey = UUID.randomUUID();
        ReservationRequestDTO request = new ReservationRequestDTO(zone.getId(), 2, idempotencyKey);

        mockMvc.perform(post("/api/v1/reservations/{customerId}", customer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].status").value("RESERVED"))
                .andExpect(jsonPath("$[0].idempotencyKey").value(idempotencyKey.toString()))
                .andExpect(jsonPath("$[1].status").value("RESERVED"));

        List<Reservation> reservations = reservationRepository.findByIdempotencyKey(idempotencyKey);
        assertThat(reservations).hasSize(2);
        assertThat(reservations).allSatisfy(reservation -> {
            assertThat(reservation.getStatus()).isEqualTo(Status.RESERVED);
            assertThat(reservation.getUser().getId()).isEqualTo(customer.getId());
            assertThat(reservation.getZone().getId()).isEqualTo(zone.getId());
            assertThat(reservation.getExpiresAt()).isAfter(Instant.now());
        });
    }

    @Test
    @DisplayName("Integration: Should not create duplicate reservations for an idempotent request")
    void reserve_RetryWithSameIdempotencyKey_DoesNotDuplicate() throws Exception {
        User customer = createCustomer();
        Zone zone = createZone(10);
        ReservationRequestDTO request = new ReservationRequestDTO(zone.getId(), 2, UUID.randomUUID());
        String jsonRequest = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/v1/reservations/{customerId}", customer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(post("/api/v1/reservations/{customerId}", customer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(2)));

        assertThat(reservationRepository.findByIdempotencyKey(request.idempotencyKey())).hasSize(2);
    }

    @Test
    @DisplayName("Integration: Should reject reservations exceeding zone capacity")
    void reserve_InsufficientCapacity_DoesNotPersistReservations() throws Exception {
        User customer = createCustomer();
        Zone zone = createZone(1);
        UUID idempotencyKey = UUID.randomUUID();
        ReservationRequestDTO request = new ReservationRequestDTO(zone.getId(), 2, idempotencyKey);

        mockMvc.perform(post("/api/v1/reservations/{customerId}", customer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        assertThat(reservationRepository.findByIdempotencyKey(idempotencyKey)).isEmpty();
    }

    @Test
    @DisplayName("Integration: Should confirm payment and persist transaction details")
    void confirmPayment_ConfirmsReservations() throws Exception {
        User customer = createCustomer();
        Zone zone = createZone(10);
        UUID idempotencyKey = UUID.randomUUID();
        ReservationRequestDTO reservationRequest = new ReservationRequestDTO(zone.getId(), 2, idempotencyKey);

        mockMvc.perform(post("/api/v1/reservations/{customerId}", customer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationRequest)))
                .andExpect(status().isCreated());

        UUID transactionId = UUID.randomUUID();
        ConfirmPaymentRequestDTO paymentRequest = new ConfirmPaymentRequestDTO(idempotencyKey, transactionId);

        mockMvc.perform(post("/api/v1/reservations/confirm-payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idempotencyKey").value(idempotencyKey.toString()))
                .andExpect(jsonPath("$.transactionId").value(transactionId.toString()))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.confirmedAt").isNotEmpty())
                .andExpect(jsonPath("$.reservations", hasSize(2)))
                .andExpect(jsonPath("$.reservations[0].status").value("CONFIRMED"));

        List<Reservation> reservations = reservationRepository.findByIdempotencyKey(idempotencyKey);
        assertThat(reservations).hasSize(2);
        assertThat(reservations).allSatisfy(reservation -> {
            assertThat(reservation.getStatus()).isEqualTo(Status.CONFIRMED);
            assertThat(reservation.getTransactionId()).isEqualTo(transactionId);
        });
    }

    @Test
    @DisplayName("Integration: Should not confirm expired reservations")
    void confirmPayment_ExpiredReservation_RemainsUnconfirmed() throws Exception {
        User customer = createCustomer();
        Zone zone = createZone(10);
        UUID idempotencyKey = UUID.randomUUID();
        Reservation expiredReservation = reservationRepository.save(Reservation.builder()
                .status(Status.RESERVED)
                .user(customer)
                .zone(zone)
                .idempotencyKey(idempotencyKey)
                .expiresAt(Instant.now().minusSeconds(60))
                .build());
        UUID transactionId = UUID.randomUUID();
        ConfirmPaymentRequestDTO paymentRequest = new ConfirmPaymentRequestDTO(idempotencyKey, transactionId);

        mockMvc.perform(post("/api/v1/reservations/confirm-payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isBadRequest());

        Reservation persistedReservation = reservationRepository.findById(expiredReservation.getId()).orElseThrow();
        assertThat(persistedReservation.getStatus()).isEqualTo(Status.RESERVED);
        assertThat(persistedReservation.getTransactionId()).isNull();
    }

    private User createCustomer() {
        return userRepository.save(User.builder()
                .firstName("Test")
                .lastName("Customer")
                .email(UUID.randomUUID() + "@example.com")
                .documentNumber(UUID.randomUUID().toString())
                .password("test-password")
                .role(Role.CUSTOMER)
                .build());
    }

    private Zone createZone(int capacity) {
        Event event = eventRepository.save(Event.builder()
                .title("Test event " + UUID.randomUUID())
                .description("Integration test event")
                .date(LocalDateTime.now().plusDays(30))
                .location("Resistencia")
                .build());

        return zoneRepository.save(Zone.builder()
                .name("General")
                .description("General admission")
                .price(new BigDecimal("1000.00"))
                .capacity(capacity)
                .event(event)
                .build());
    }
}
