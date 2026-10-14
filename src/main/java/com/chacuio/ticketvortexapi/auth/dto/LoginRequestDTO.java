package com.chacuio.ticketvortexapi.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record LoginRequestDTO(
        @Email(message = "The email field must have a valid format")
        @NotBlank(message = "The email field is required")
        String email,

        @NotBlank(message = "The password field is required")
        @Length(min = 8, max = 120, message = "The password field must be between 1 and 120 characters long")
        String password
) {}
