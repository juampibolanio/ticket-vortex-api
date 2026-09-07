package com.chacuio.ticketvortexapi.user.dto;

import com.chacuio.ticketvortexapi.user.model.Role;

import java.time.Instant;
import java.util.UUID;

public record UserResponseDTO(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String documentNumber,
        Role role,
        boolean isActive,
        Instant createdAt,
        Instant updatedAt
) {}
