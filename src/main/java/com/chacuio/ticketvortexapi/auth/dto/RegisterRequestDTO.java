package com.chacuio.ticketvortexapi.auth.dto;

public record RegisterRequestDTO(
        String firstName,
        String lastName,
        String email,
        String documentNumber,
        String password
) { }
