package com.chacuio.ticketvortexapi.reservation.exception;

import com.chacuio.ticketvortexapi.common.exceptions.BadRequestException;

public class NotEnoughCapacityException extends BadRequestException {
    public NotEnoughCapacityException(String message) {
        super(message);
    }
}
