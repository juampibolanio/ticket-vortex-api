package com.chacuio.ticketvortexapi.reservation.task;

import com.chacuio.ticketvortexapi.reservation.model.Reservation;
import com.chacuio.ticketvortexapi.reservation.model.Status;
import com.chacuio.ticketvortexapi.reservation.repository.ReservationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationExpirationTaskTest {

    @Mock
    private ReservationRepository repository;

    @InjectMocks
    private ReservationExpirationTask task;

    @Test
    @DisplayName("Should return early and not save when there are no expired reservations")
    void expirePendingReservations_WhenNoExpired_DoesNotSave() {
        when(repository.findByStatusAndExpiresAtBefore(eq(Status.RESERVED), any(Instant.class)))
                .thenReturn(Collections.emptyList());

        task.expirePendingReservations();

        verify(repository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Should update status to EXPIRED and save all when expired reservations exist")
    void expirePendingReservations_WhenExpiredExist_UpdatesStatusAndSavesAll() {
        Reservation reservation1 = Reservation.builder().status(Status.RESERVED).build();
        Reservation reservation2 = Reservation.builder().status(Status.RESERVED).build();
        List<Reservation> expiredList = List.of(reservation1, reservation2);

        when(repository.findByStatusAndExpiresAtBefore(eq(Status.RESERVED), any(Instant.class)))
                .thenReturn(expiredList);

        task.expirePendingReservations();

        assertEquals(Status.EXPIRED, reservation1.getStatus());
        assertEquals(Status.EXPIRED, reservation2.getStatus());
        verify(repository).saveAll(expiredList);
    }
}
