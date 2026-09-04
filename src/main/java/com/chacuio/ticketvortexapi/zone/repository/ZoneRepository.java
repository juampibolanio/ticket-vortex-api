package com.chacuio.ticketvortexapi.zone.repository;

import com.chacuio.ticketvortexapi.zone.model.Zone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ZoneRepository extends JpaRepository<Zone, UUID> { }
