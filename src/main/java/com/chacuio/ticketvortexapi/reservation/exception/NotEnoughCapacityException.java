package com.chacuio.ticketvortexapi.reservation.exception;

public class NotEnoughCapacityException extends RuntimeException {
    public NotEnoughCapacityException(String message) {
        super(message);
    }
}
