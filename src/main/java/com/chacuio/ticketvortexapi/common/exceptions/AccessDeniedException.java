package com.chacuio.ticketvortexapi.common.exceptions;

public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) {
        super(message);
    }

    public AccessDeniedException() {
        super("Access denied");
    }
}
