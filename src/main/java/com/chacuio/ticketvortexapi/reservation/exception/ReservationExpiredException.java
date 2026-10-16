package com.chacuio.ticketvortexapi.reservation.exception;

import com.chacuio.ticketvortexapi.common.exceptions.BadRequestException;

public class ReservationExpiredException extends BadRequestException {
    public ReservationExpiredException(String message) {
        super(message);
    }
}
