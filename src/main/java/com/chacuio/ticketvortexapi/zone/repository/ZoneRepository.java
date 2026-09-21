package com.chacuio.ticketvortexapi.zone.repository;

import com.chacuio.ticketvortexapi.zone.dto.ZoneResponseDTO;
import com.chacuio.ticketvortexapi.zone.dto.ZoneSummaryDTO;
import com.chacuio.ticketvortexapi.zone.model.Zone;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ZoneRepository extends JpaRepository<Zone, UUID> {

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("SELECT z FROM Zone z WHERE z.id = :id")
    Optional<Zone> findByIdWithLock(@Param("id") UUID id);

    @Query("""
            SELECT new com.chacuio.ticketvortexapi.zone.dto.ZoneSummaryDTO(
                z.id,
                z.name,
                z.capacity,
                z.event.id
            )
            FROM Zone z
            WHERE z.event.id = :event_id
        """)
    List<ZoneSummaryDTO> findAllByEventId(@Param("event_id") UUID eventId);

    @Query("SELECT z FROM Zone z JOIN FETCH z.event WHERE z.id = :zone_id")
    Optional<Zone> findByIdWithEvent(@Param("zone_id") UUID zoneId);
}
