package com.chacuio.ticketvortexapi.auth.dto;

import com.chacuio.ticketvortexapi.user.model.Role;

import java.time.Instant;
import java.util.UUID;

public record LoginResponseDTO(
        String token,
        Instant expiresIn,
        UUID userId,
        String email,
        Role role
) {}
