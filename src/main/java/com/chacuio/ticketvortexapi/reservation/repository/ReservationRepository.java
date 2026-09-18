package com.chacuio.ticketvortexapi.reservation.repository;

import com.chacuio.ticketvortexapi.reservation.dto.ReservationSummaryDTO;
import com.chacuio.ticketvortexapi.reservation.model.Reservation;
import com.chacuio.ticketvortexapi.reservation.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    boolean existsByIdempotencyKey(UUID uuid);

    @Query("""
        SELECT new com.chacuio.ticketvortexapi.reservation.dto.ReservationSummaryDTO(
            r.id,
            u.id,
            u.email,
            z.id,
            z.name,
            r.status,
            z.price,
            r.expiresAt,
            r.createdAt
            )
        FROM Reservation r
        JOIN r.user u 
        JOIN r.zone z
        JOIN z.event e
    """)
    List<ReservationSummaryDTO> findAllSummarized();

    List<Reservation> findByIdempotencyKey(UUID uuid);

    List<Reservation> findByStatusAndExpiresAtBefore(Status status, Instant currentTime);

    @Query("SELECT COUNT(r) FROM Reservation r " +
            "WHERE r.zone.id = :zone_id " +
            "AND (r.status = :status_confirmed OR (r.status = :status_reserved AND r.expiresAt > :current_time))")
    Long countUnavailablePlacesByZone(
            @Param("zone_id") UUID zoneId,
            @Param("current_time") Instant currentTime,
            @Param("status_confirmed") Status statusConfirmed,
            @Param("status_reserved") Status statusReserved
    );

    @Query("""
        SELECT COUNT(r)
        FROM Reservation r
        JOIN r.zone z
        WHERE r.user.id = :user_id
            AND z.event.id = :event_id
            AND (
                r.status = :status_confirmed
                    OR (r.status = :status_reserved AND r.expiresAt > :current_time)
                )
    """)
    Long countActiveReservationsForUserAndEvent(
            @Param("user_id") UUID userId,
            @Param("event_id") UUID eventId,
            @Param("current_time") Instant currentTime,
            @Param("status_confirmed") Status statusConfirmed,
            @Param("status_reserved") Status statusReserved
            );
}
