package com.chacuio.ticketvortexapi.security.jwt;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
        @NotBlank(message = "JWT Secret is required")
        String secret,

        @Positive(message = "Expiration time must be greater than zero")
        long expiration,

        @Positive(message = "Renew time must be greater than zero")
        long renewTime
) {
}
