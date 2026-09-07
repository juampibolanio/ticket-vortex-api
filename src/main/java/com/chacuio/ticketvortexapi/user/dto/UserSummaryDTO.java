package com.chacuio.ticketvortexapi.user.dto;

import java.util.UUID;

public record UserSummaryDTO(
        UUID id,
        String firstName,
        String lastName,
        String email
) {}
