package com.chacuio.ticketvortexapi.common.exceptions;

public abstract class BusinessException extends RuntimeException {
    protected BusinessException(String message) {
        super(message);
    }
}
