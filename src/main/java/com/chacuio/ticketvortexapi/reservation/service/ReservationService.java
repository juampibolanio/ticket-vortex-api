package com.chacuio.ticketvortexapi.reservation.service;

import com.chacuio.ticketvortexapi.reservation.dto.*;

import java.util.List;
import java.util.UUID;

public interface ReservationService {
    List<ReservationSummaryDTO> findAll();
    ReservationResponseDTO findById(UUID id);
    List<ReservationResponseDTO> reserve(ReservationRequestDTO dto, UUID customerId);
    ConfirmPaymentResponseDTO confirmPayment(ConfirmPaymentRequestDTO dto);
    void delete(UUID id);
}
