package com.chacuio.ticketvortexapi.security.exception;

public class TokenCannotRenewedException extends RuntimeException {
    public TokenCannotRenewedException(String message) {
        super(message);
    }
}
