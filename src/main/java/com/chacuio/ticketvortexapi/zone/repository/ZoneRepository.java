package com.chacuio.ticketvortexapi.zone.repository;

import com.chacuio.ticketvortexapi.zone.model.Zone;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ZoneRepository extends JpaRepository<Zone, UUID> {

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("SELECT z FROM Zone z WHERE z.id = :id")
    Optional<Zone> findByIdWithLock(@Param("id") UUID id);
}
