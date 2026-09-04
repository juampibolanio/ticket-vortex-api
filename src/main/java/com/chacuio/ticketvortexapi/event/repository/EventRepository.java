package com.chacuio.ticketvortexapi.event.repository;

import com.chacuio.ticketvortexapi.event.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> { }
