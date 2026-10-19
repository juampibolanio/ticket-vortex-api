package com.chacuio.ticketvortexapi.reservation.service;

import com.chacuio.ticketvortexapi.reservation.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ReservationService {
    Page<ReservationSummaryDTO> findAllByEventId(UUID eventId, Pageable pageable);
    Page<ReservationSummaryDTO> findAllByCustomerId(UUID customerId, Pageable pageable);
    ReservationResponseDTO findById(UUID id);
    List<ReservationResponseDTO> reserve(ReservationRequestDTO dto, UUID customerId);
    ConfirmPaymentResponseDTO confirmPayment(ConfirmPaymentRequestDTO dto);
    void delete(UUID id);
}
