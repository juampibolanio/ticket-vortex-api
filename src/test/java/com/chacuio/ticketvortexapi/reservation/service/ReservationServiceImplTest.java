package com.chacuio.ticketvortexapi.reservation.service;

import com.chacuio.ticketvortexapi.common.exceptions.ResourceNotFoundException;
import com.chacuio.ticketvortexapi.event.model.Event;
import com.chacuio.ticketvortexapi.reservation.dto.ConfirmPaymentRequestDTO;
import com.chacuio.ticketvortexapi.reservation.dto.ConfirmPaymentResponseDTO;
import com.chacuio.ticketvortexapi.reservation.dto.ReservationRequestDTO;
import com.chacuio.ticketvortexapi.reservation.dto.ReservationResponseDTO;
import com.chacuio.ticketvortexapi.reservation.dto.ReservationSummaryDTO;
import com.chacuio.ticketvortexapi.reservation.exception.NotEnoughCapacityException;
import com.chacuio.ticketvortexapi.reservation.exception.ReservationExpiredException;
import com.chacuio.ticketvortexapi.reservation.exception.TicketLimitExceededException;
import com.chacuio.ticketvortexapi.reservation.mapper.ReservationMapper;
import com.chacuio.ticketvortexapi.reservation.model.Reservation;
import com.chacuio.ticketvortexapi.reservation.model.Status;
import com.chacuio.ticketvortexapi.reservation.repository.ReservationRepository;
import com.chacuio.ticketvortexapi.user.model.User;
import com.chacuio.ticketvortexapi.user.repository.UserRepository;
import com.chacuio.ticketvortexapi.zone.model.Zone;
import com.chacuio.ticketvortexapi.zone.repository.ZoneRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {
    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ZoneRepository zoneRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReservationMapper reservationMapper;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    @Test
    @DisplayName("Should find all reservation summaries by event")
    void findAllByEventId_ReturnsReservations() {
        UUID eventId = UUID.randomUUID();
        List<ReservationSummaryDTO> expected = List.of(summary());
        when(reservationRepository.findAllSummarizedByEventId(eventId)).thenReturn(expected);

        assertEquals(expected, reservationService.findAllByEventId(eventId));

        verify(reservationRepository).findAllSummarizedByEventId(eventId);
    }

    @Test
    @DisplayName("Should find all reservation summaries by customer")
    void findAllByCustomerId_ReturnsReservations() {
        UUID customerId = UUID.randomUUID();
        List<ReservationSummaryDTO> expected = List.of(summary());
        when(reservationRepository.findAllSummarizedByCustomerId(customerId)).thenReturn(expected);

        assertEquals(expected, reservationService.findAllByCustomerId(customerId));

        verify(reservationRepository).findAllSummarizedByCustomerId(customerId);
    }

    @Test
    @DisplayName("Should return a reservation by ID")
    void findById_ReturnsReservation() {
        UUID reservationId = UUID.randomUUID();
        Reservation reservation = reservation(reservationId, Status.RESERVED, Instant.now().plusSeconds(60));
        ReservationResponseDTO expected = mock(ReservationResponseDTO.class);
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(reservationMapper.toDto(reservation)).thenReturn(expected);

        assertSame(expected, reservationService.findById(reservationId));

        verify(reservationRepository).findById(reservationId);
        verify(reservationMapper).toDto(reservation);
    }

    @Test
    @DisplayName("Should throw when the requested reservation does not exist")
    void findById_NotFound_ThrowsException() {
        UUID reservationId = UUID.randomUUID();
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> reservationService.findById(reservationId)
        );

        assertEquals("Reservation with id: " + reservationId + " not found", exception.getMessage());
        verifyNoInteractions(reservationMapper);
    }

    @Test
    @DisplayName("Should return existing reservations when reserve is retried with the same key")
    void reserve_IdempotentRequest_ReturnsExistingReservations() {
        ReservationRequestDTO request = request(UUID.randomUUID(), 2);
        UUID customerId = UUID.randomUUID();
        Reservation existing = reservation(UUID.randomUUID(), Status.RESERVED, Instant.now().plusSeconds(60));
        ReservationResponseDTO expectedResponse = mock(ReservationResponseDTO.class);
        when(reservationRepository.existsByIdempotencyKey(request.idempotencyKey())).thenReturn(true);
        when(reservationRepository.findByIdempotencyKey(request.idempotencyKey())).thenReturn(List.of(existing));
        when(reservationMapper.toDto(existing)).thenReturn(expectedResponse);

        assertEquals(List.of(expectedResponse), reservationService.reserve(request, customerId));

        verify(reservationRepository).findByIdempotencyKey(request.idempotencyKey());
        verifyNoInteractions(zoneRepository, userRepository);
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw when the reservation zone does not exist")
    void reserve_ZoneNotFound_ThrowsException() {
        ReservationRequestDTO request = request(UUID.randomUUID(), 1);
        when(reservationRepository.existsByIdempotencyKey(request.idempotencyKey())).thenReturn(false);
        when(zoneRepository.findByIdWithLock(request.zoneId())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> reservationService.reserve(request, UUID.randomUUID())
        );

        assertEquals("Zone not found", exception.getMessage());
        verifyNoInteractions(userRepository);
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw when the customer does not exist")
    void reserve_CustomerNotFound_ThrowsException() {
        ReservationRequestDTO request = request(UUID.randomUUID(), 1);
        UUID customerId = UUID.randomUUID();
        Zone zone = zone(5, UUID.randomUUID());
        when(reservationRepository.existsByIdempotencyKey(request.idempotencyKey())).thenReturn(false);
        when(zoneRepository.findByIdWithLock(request.zoneId())).thenReturn(Optional.of(zone));
        when(userRepository.findById(customerId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> reservationService.reserve(request, customerId)
        );

        assertEquals("Customer not found", exception.getMessage());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should enforce the maximum number of active event tickets")
    void reserve_TicketLimitExceeded_ThrowsException() {
        ReservationRequestDTO request = request(UUID.randomUUID(), 2);
        UUID customerId = UUID.randomUUID();
        Zone zone = zone(10, UUID.randomUUID());
        when(reservationRepository.existsByIdempotencyKey(request.idempotencyKey())).thenReturn(false);
        when(zoneRepository.findByIdWithLock(request.zoneId())).thenReturn(Optional.of(zone));
        when(userRepository.findById(customerId)).thenReturn(Optional.of(new User()));
        when(reservationRepository.countActiveReservationsForUserAndEvent(
                eq(customerId), eq(zone.getEvent().getId()), any(Instant.class),
                eq(Status.CONFIRMED), eq(Status.RESERVED))).thenReturn(3L);

        assertThrows(
                TicketLimitExceededException.class,
                () -> reservationService.reserve(request, customerId)
        );

        verify(reservationRepository, never()).countUnavailablePlacesByZone(
                any(), any(Instant.class), any(), any());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw when the zone does not have enough available capacity")
    void reserve_NotEnoughCapacity_ThrowsException() {
        ReservationRequestDTO request = request(UUID.randomUUID(), 2);
        UUID customerId = UUID.randomUUID();
        Zone zone = zone(3, UUID.randomUUID());
        when(reservationRepository.existsByIdempotencyKey(request.idempotencyKey())).thenReturn(false);
        when(zoneRepository.findByIdWithLock(request.zoneId())).thenReturn(Optional.of(zone));
        when(userRepository.findById(customerId)).thenReturn(Optional.of(new User()));
        when(reservationRepository.countActiveReservationsForUserAndEvent(
                eq(customerId), eq(zone.getEvent().getId()), any(Instant.class),
                eq(Status.CONFIRMED), eq(Status.RESERVED))).thenReturn(0L);
        when(reservationRepository.countUnavailablePlacesByZone(
                eq(zone.getId()), any(Instant.class), eq(Status.CONFIRMED), eq(Status.RESERVED))).thenReturn(2L);

        assertThrows(
                NotEnoughCapacityException.class,
                () -> reservationService.reserve(request, customerId)
        );

        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should create the requested number of reservations")
    void reserve_Success() {
        ReservationRequestDTO request = request(UUID.randomUUID(), 2);
        UUID customerId = UUID.randomUUID();
        User user = new User();
        Zone zone = zone(5, UUID.randomUUID());
        ReservationResponseDTO expectedResponse = mock(ReservationResponseDTO.class);
        when(reservationRepository.existsByIdempotencyKey(request.idempotencyKey())).thenReturn(false);
        when(zoneRepository.findByIdWithLock(request.zoneId())).thenReturn(Optional.of(zone));
        when(userRepository.findById(customerId)).thenReturn(Optional.of(user));
        when(reservationRepository.countActiveReservationsForUserAndEvent(
                eq(customerId), eq(zone.getEvent().getId()), any(Instant.class),
                eq(Status.CONFIRMED), eq(Status.RESERVED))).thenReturn(1L);
        when(reservationRepository.countUnavailablePlacesByZone(
                eq(zone.getId()), any(Instant.class), eq(Status.CONFIRMED), eq(Status.RESERVED))).thenReturn(1L);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reservationMapper.toDto(any(Reservation.class))).thenReturn(expectedResponse);

        List<ReservationResponseDTO> result = reservationService.reserve(request, customerId);

        assertEquals(List.of(expectedResponse, expectedResponse), result);
        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository, times(2)).save(captor.capture());
        for (Reservation saved : captor.getAllValues()) {
            assertEquals(Status.RESERVED, saved.getStatus());
            assertSame(user, saved.getUser());
            assertSame(zone, saved.getZone());
            assertEquals(request.idempotencyKey(), saved.getIdempotencyKey());
            assertNotNull(saved.getExpiresAt());
            assertTrue(saved.getExpiresAt().isAfter(Instant.now()));
            assertTrue(saved.getExpiresAt().isBefore(Instant.now().plusSeconds(11 * 60)));
        }
        verify(reservationMapper, times(2)).toDto(any(Reservation.class));
    }

    @Test
    @DisplayName("Should confirm all reservations and return their summaries")
    void confirmPayment_Success() {
        UUID idempotencyKey = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        ConfirmPaymentRequestDTO request = new ConfirmPaymentRequestDTO(idempotencyKey, transactionId);
        Reservation first = reservation(UUID.randomUUID(), Status.RESERVED, Instant.now().plusSeconds(60));
        Reservation second = reservation(UUID.randomUUID(), Status.RESERVED, Instant.now().plusSeconds(60));
        ReservationSummaryDTO firstSummary = summary();
        ReservationSummaryDTO secondSummary = summary();
        when(reservationRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(false);
        when(reservationRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(List.of(first, second));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reservationMapper.toSummaryDto(first)).thenReturn(firstSummary);
        when(reservationMapper.toSummaryDto(second)).thenReturn(secondSummary);

        ConfirmPaymentResponseDTO result = reservationService.confirmPayment(request);

        assertEquals(idempotencyKey, result.idempotencyKey());
        assertEquals(transactionId, result.transactionId());
        assertEquals(Status.CONFIRMED, result.status());
        assertNotNull(result.confirmedAt());
        assertEquals(List.of(firstSummary, secondSummary), result.reservations());
        for (Reservation confirmed : List.of(first, second)) {
            assertEquals(Status.CONFIRMED, confirmed.getStatus());
            assertEquals(transactionId, confirmed.getTransactionId());
        }
        verify(reservationRepository, times(2)).save(any(Reservation.class));
    }

    @Test
    @DisplayName("Should return already confirmed reservations without confirming them again")
    void confirmPayment_AlreadyConfirmed_ReturnsExistingReservations() {
        UUID idempotencyKey = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        ConfirmPaymentRequestDTO request = new ConfirmPaymentRequestDTO(idempotencyKey, transactionId);
        Reservation existing = reservation(UUID.randomUUID(), Status.CONFIRMED, Instant.now().minusSeconds(60));
        ReservationSummaryDTO expectedSummary = summary();
        when(reservationRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(true);
        when(reservationRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(List.of(existing));
        when(reservationMapper.toSummaryDto(existing)).thenReturn(expectedSummary);

        ConfirmPaymentResponseDTO result = reservationService.confirmPayment(request);

        assertEquals(idempotencyKey, result.idempotencyKey());
        assertEquals(transactionId, result.transactionId());
        assertNull(result.status());
        assertNull(result.confirmedAt());
        assertEquals(List.of(expectedSummary), result.reservations());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw when no reservations exist for the payment idempotency key")
    void confirmPayment_NoReservations_ThrowsException() {
        UUID idempotencyKey = UUID.randomUUID();
        ConfirmPaymentRequestDTO request = new ConfirmPaymentRequestDTO(idempotencyKey, UUID.randomUUID());
        when(reservationRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(false);
        when(reservationRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(List.of());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> reservationService.confirmPayment(request)
        );

        assertEquals("Reservations with idempotency id: " + idempotencyKey + " not found", exception.getMessage());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject payment when a reservation has expired")
    void confirmPayment_ExpiredReservation_ThrowsException() {
        UUID idempotencyKey = UUID.randomUUID();
        ConfirmPaymentRequestDTO request = new ConfirmPaymentRequestDTO(idempotencyKey, UUID.randomUUID());
        Reservation expired = reservation(UUID.randomUUID(), Status.RESERVED, Instant.now().minusSeconds(1));
        when(reservationRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(false);
        when(reservationRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(List.of(expired));

        assertThrows(
                ReservationExpiredException.class,
                () -> reservationService.confirmPayment(request)
        );

        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete an existing reservation")
    void delete_Success() {
        UUID reservationId = UUID.randomUUID();
        Reservation reservation = reservation(reservationId, Status.RESERVED, Instant.now().plusSeconds(60));
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        reservationService.delete(reservationId);

        verify(reservationRepository).delete(reservation);
    }

    @Test
    @DisplayName("Should throw when deleting a reservation that does not exist")
    void delete_NotFound_ThrowsException() {
        UUID reservationId = UUID.randomUUID();
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> reservationService.delete(reservationId)
        );

        assertEquals("Reservation with id: " + reservationId + " not found", exception.getMessage());
        verify(reservationRepository, never()).delete(any(Reservation.class));
    }

    private static ReservationRequestDTO request(UUID idempotencyKey, int quantity) {
        return new ReservationRequestDTO(UUID.randomUUID(), quantity, idempotencyKey);
    }

    private static ReservationSummaryDTO summary() {
        return new ReservationSummaryDTO(
                UUID.randomUUID(), UUID.randomUUID(), "customer@example.com",
                UUID.randomUUID(), "VIP", Status.RESERVED, null, Instant.now(), Instant.now()
        );
    }

    private static Reservation reservation(UUID id, Status status, Instant expiresAt) {
        return Reservation.builder()
                .id(id)
                .status(status)
                .user(new User())
                .zone(new Zone())
                .expiresAt(expiresAt)
                .build();
    }

    private static Zone zone(int capacity, UUID eventId) {
        Event event = new Event();
        event.setId(eventId);
        return Zone.builder()
                .id(UUID.randomUUID())
                .capacity(capacity)
                .event(event)
                .build();
    }
}
