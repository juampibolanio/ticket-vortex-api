package com.chacuio.ticketvortexapi.reservation.repository;

import com.chacuio.ticketvortexapi.reservation.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    @Query("SELECT COUNT(r) FROM Reservation r " +
            "WHERE r.zone.id = :zone_id " +
            "AND (r.status = :statusConfirmed OR (r.status = :statusReserved AND r.expiresAt > :current_time))")
    Long countUnavailablePlacesByZone(
            @Param("zone_id") UUID zoneId,
            @Param("current_time") Instant currentTime);
}
