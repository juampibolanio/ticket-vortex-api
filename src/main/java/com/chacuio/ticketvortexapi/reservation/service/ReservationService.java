package com.chacuio.ticketvortexapi.reservation.service;

import com.chacuio.ticketvortexapi.reservation.dto.*;

import java.util.List;
import java.util.UUID;

public interface ReservationService {
    List<ReservationSummaryDTO> findAll();
    ReservationResponseDTO findById(UUID id);
    ReservationResponseDTO reserve(ReservationRequestDTO dto);
    ConfirmPaymentResponseDTO confirmPayment(ConfirmPaymentRequestDTO dto);
    void delete(UUID id);
}
