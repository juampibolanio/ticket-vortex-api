package com.chacuio.ticketvortexapi.security.exception;

import io.jsonwebtoken.JwtException;

public class InvalidOrMalFormedTokenException extends JwtException {
    public InvalidOrMalFormedTokenException(String message) {
        super(message);
    }
}
