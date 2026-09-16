package com.chacuio.ticketvortexapi.reservation.mapper;

import com.chacuio.ticketvortexapi.reservation.dto.ReservationRequestDTO;
import com.chacuio.ticketvortexapi.reservation.dto.ReservationResponseDTO;
import com.chacuio.ticketvortexapi.reservation.dto.ReservationSummaryDTO;
import com.chacuio.ticketvortexapi.reservation.model.Reservation;
import com.chacuio.ticketvortexapi.reservation.model.Status;
import com.chacuio.ticketvortexapi.user.mapper.UserMapper;
import com.chacuio.ticketvortexapi.user.model.User;
import com.chacuio.ticketvortexapi.zone.mapper.ZoneMapper;
import com.chacuio.ticketvortexapi.zone.model.Zone;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class ReservationMapper {
    private final UserMapper userMapper;
    private final ZoneMapper zoneMapper;

    public ReservationResponseDTO toDto(Reservation reservation) {
        return new ReservationResponseDTO(
                reservation.getId(),
                userMapper.toSummaryDto(reservation.getUser()),
                zoneMapper.toDtoSummary(reservation.getZone()),
                reservation.getStatus(),
                reservation.getExpiresAt(),
                reservation.getIdempotencyKey(),
                reservation.getCreatedAt(),
                reservation.getUpdatedAt()
        );
    }

    public Reservation toEntity(ReservationRequestDTO dto, Zone zone, User user, Instant expiresAt) {
        return Reservation.builder()
                .status(Status.RESERVED)
                .zone(zone)
                .user(user)
                .idempotencyKey(dto.idempotencyKey())
                .expiresAt(expiresAt)
                .build();
    }

    public ReservationSummaryDTO toSummaryDto(Reservation reservation) {
        return new ReservationSummaryDTO(
                reservation.getId(),
                reservation.getUser().getId(),
                reservation.getUser().getEmail(),
                reservation.getZone().getId(),
                reservation.getZone().getName(),
                reservation.getStatus(),
                reservation.getZone().getPrice(),
                reservation.getExpiresAt(),
                reservation.getCreatedAt()
        );
    }
}
