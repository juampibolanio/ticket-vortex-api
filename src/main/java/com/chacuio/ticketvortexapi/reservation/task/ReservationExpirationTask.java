package com.chacuio.ticketvortexapi.reservation.task;

import com.chacuio.ticketvortexapi.reservation.model.Reservation;
import com.chacuio.ticketvortexapi.reservation.model.Status;
import com.chacuio.ticketvortexapi.reservation.repository.ReservationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationExpirationTask {
    private final ReservationRepository repository;

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    @Transactional
    public void expirePendingReservations() {
        Instant now = Instant.now(Clock.systemUTC());

        List<Reservation> expiredReservations = repository.findByStatusAndExpiresAtBefore(Status.RESERVED, now);

        if (expiredReservations.isEmpty()) {
            log.info("Found {} expired reservations. Expiring them now...", expiredReservations.size());

            for (Reservation reservation : expiredReservations) {
                reservation.setStatus(Status.EXPIRED);
            }

            repository.saveAll(expiredReservations);
        }
    }
}
