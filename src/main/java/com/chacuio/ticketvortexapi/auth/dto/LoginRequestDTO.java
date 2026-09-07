package com.chacuio.ticketvortexapi.auth.dto;

public record LoginRequestDTO(
        String email,
        String password
) {}
