package com.chacuio.ticketvortexapi.user.dto;

import com.chacuio.ticketvortexapi.user.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserPatchRequestDTO(
        @Size(min = 1, max = 120, message = "The first name field must be between 1 and 120 characters long")
        String firstName,

        @Size(min = 1, max = 120, message = "The last name field must be between 1 and 120 characters long")
        String lastName,

        @Email(message = "The email field must have a valid format")
        @Size(max = 255, message = "The email field cannot exceed 254 characters")
        String email,

        @Size(min = 1, max = 50, message = "The document number field must be between 1 and 50 characters long")
        String documentNumber,

        Role role
) { }
