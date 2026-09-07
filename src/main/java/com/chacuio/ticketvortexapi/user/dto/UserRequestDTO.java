package com.chacuio.ticketvortexapi.user.dto;

import com.chacuio.ticketvortexapi.user.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequestDTO(
        @NotBlank(message = "The first name field is required")
        @Size(min = 1, max = 120, message = "The first name field must be between 1 and 120 characters long")
        String firstName,

        @NotBlank(message = "The last name field is required")
        @Size(min = 1, max = 120, message = "The last name field must be between 1 and 120 characters long")
        String lastName,

        @Email(message = "The email field must have a valid format")
        @NotBlank(message = "The email field is required")
        @Size(max = 255, message = "The email field cannot exceed 254 characters")
        String email,

        @NotBlank(message = "The document number field is required")
        @Size(min = 1, max = 50, message = "The document number field must be between 1 and 50 characters long")
        String documentNumber,

        @NotBlank(message = "The role field is required")
        Role role,

        @NotBlank(message = "The password field is required")
        String password
) {}
