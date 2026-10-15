package com.chacuio.ticketvortexapi.security.exception;

public class InvalidOrMalFormedTokenException extends RuntimeException {
    public InvalidOrMalFormedTokenException(String message) {
        super(message);
    }
}
