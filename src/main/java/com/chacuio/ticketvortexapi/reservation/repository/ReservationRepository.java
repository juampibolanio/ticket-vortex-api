package com.chacuio.ticketvortexapi.reservation.repository;

import com.chacuio.ticketvortexapi.reservation.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> { }
