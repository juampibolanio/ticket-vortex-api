package com.chacuio.ticketvortexapi.auth.dto;

import com.chacuio.ticketvortexapi.user.dto.UserResponseDTO;

public record RegisterResponseDTO(
        UserResponseDTO userData,
        String token
){}
