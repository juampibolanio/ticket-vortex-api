package com.chacuio.ticketvortexapi.reservation.exception;

public class TicketLimitExceededException extends RuntimeException {
    public TicketLimitExceededException(String message) {
        super(message);
    }
}
