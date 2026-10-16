package com.chacuio.ticketvortexapi.security.exception;

import io.jsonwebtoken.JwtException;

public class TokenCannotRenewedException extends JwtException {
    public TokenCannotRenewedException(String message) {
        super(message);
    }
}
