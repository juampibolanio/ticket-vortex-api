package com.chacuio.ticketvortexapi.reservation.exception;

import com.chacuio.ticketvortexapi.common.exceptions.BadRequestException;

public class TicketLimitExceededException extends BadRequestException {
    public TicketLimitExceededException(String message) {
        super(message);
    }
}
