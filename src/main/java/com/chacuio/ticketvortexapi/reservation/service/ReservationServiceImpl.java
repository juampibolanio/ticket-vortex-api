package com.chacuio.ticketvortexapi.reservation.service;

import com.chacuio.ticketvortexapi.common.exceptions.ResourceNotFoundException;
import com.chacuio.ticketvortexapi.reservation.dto.*;
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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {
    private final ReservationRepository reservationRep;
    private final ZoneRepository zoneRep;
    private final UserRepository userRep;
    private final ReservationMapper reservationMapper;

    @Override
    public List<ReservationSummaryDTO> findAll() {
        return reservationRep.findAllSummarized();
    }

    @Override
    public ReservationResponseDTO findById(UUID id) {
        return reservationMapper.toDto(reservationRep.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation with id: " + id + " not found")));
    }

    @Transactional
    @Override
    public List<ReservationResponseDTO> reserve(ReservationRequestDTO dto, UUID customerId) {
        // verify if exists reservations with idempotency key from dto
        if (reservationRep.existsByIdempotencyKey(dto.idempotencyKey())) {
            List<Reservation> reservations =  reservationRep.findByIdempotencyKey(dto.idempotencyKey());

            return reservations.stream()
                    .map(reservationMapper::toDto)
                    .toList();
        }

        // search zone
        Zone zone = zoneRep.findByIdWithLock(dto.zoneId())
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found"));

        // search customer
        User user = userRep.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        // Check if the customer has more than 4 reservations with a status of "reserved" or "confirmed."
        Long activeReserves = reservationRep.countActiveReservationsForUserAndEvent(
                customerId,
                zone.getId(),
                Instant.now(Clock.systemUTC()),
                Status.CONFIRMED,
                Status.RESERVED);

        if (activeReserves + dto.quantity() > 4) {
            throw new TicketLimitExceededException("The customer has exceeded the maximum limit of 4 tickets for this event.");
        }

        // calculate available capacity
        Long unavailablePlaces = reservationRep.countUnavailablePlacesByZone(zone.getId(), Instant.now(Clock.systemUTC()), Status.CONFIRMED, Status.RESERVED);
        long availablePlaces = zone.getCapacity().longValue() - unavailablePlaces;

        if (availablePlaces < dto.quantity()) {
            throw new NotEnoughCapacityException("Not enough capacity");
        }

        // create reservations
        List<ReservationResponseDTO> reservations = new ArrayList<>();
        for (int i = 0; i < dto.quantity(); i++) {
            Reservation reservation = Reservation.builder()
                    .status(Status.RESERVED)
                    .user(user)
                    .zone(zone)
                    .idempotencyKey(dto.idempotencyKey())
                    .expiresAt(Instant.now(Clock.systemUTC()).plus(10, ChronoUnit.MINUTES))
                    .build();
            Reservation savedReservation = reservationRep.save(reservation);
            reservations.add(reservationMapper.toDto(savedReservation));
        }

        return reservations;
    }

    @Transactional
    @Override
    public ConfirmPaymentResponseDTO confirmPayment(ConfirmPaymentRequestDTO dto) {
        // check if exists reservations with dto idempotency key
        if (reservationRep.existsByIdempotencyKey(dto.idempotencyKey())) {
            List<Reservation> reservations =  reservationRep.findByIdempotencyKey(dto.idempotencyKey());

            // check if exists reservations with idempotency key
            if (!reservations.isEmpty()) {

                // check if reservations with idempotency key are confirmed
                boolean alreadyConfirmed = reservations.stream()
                        .allMatch(reservation -> reservation.getStatus().equals(Status.CONFIRMED));

                if (alreadyConfirmed) {
                    List<ReservationSummaryDTO> mappedReservations = reservations.stream()
                            .map(reservationMapper::toSummaryDto)
                            .toList();

                    return new ConfirmPaymentResponseDTO(
                            dto.idempotencyKey(),
                            dto.transactionId(),
                            null,
                            null,
                            mappedReservations
                    );
                }
            }
        }

        // continue to confirm payment logic
        List<Reservation> reservations = reservationRep.findByIdempotencyKey(dto.idempotencyKey());

        if (!reservations.isEmpty()) {
            throw new ResourceNotFoundException("Reservations with idempotency id: " + dto.idempotencyKey() + " not found");
        }

        // check if the reservations have not expired
        Instant now = Instant.now(Clock.systemUTC());

        boolean isExpired = reservations.stream()
                .anyMatch(reservation -> reservation.getExpiresAt().isBefore(now));

        if (isExpired) {
            throw new ReservationExpiredException("One or more reservations have expired");
        }

        List<ReservationSummaryDTO> confirmedReservations = new ArrayList<>();
        for (Reservation reservation : reservations) {
            reservation.setTransactionId(dto.transactionId());
            reservation.setStatus(Status.CONFIRMED);

            confirmedReservations.add(reservationMapper.toSummaryDto(reservationRep.save(reservation))) ;
            log.info("[RESERVATIONS]: Reservation with id: {} has been confirmed.", reservation.getId());
        }

        return new ConfirmPaymentResponseDTO(
                dto.idempotencyKey(),
                dto.transactionId(),
                Status.CONFIRMED,
                Instant.now(Clock.systemUTC()),
                confirmedReservations
        );
    }

    @Override
    public void delete(UUID id) {
        Reservation reservation = reservationRep.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation with id: " + id + " not found"));

        reservationRep.delete(reservation);
    }

}
