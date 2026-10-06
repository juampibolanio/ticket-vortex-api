package com.chacuio.ticketvortexapi.user.dto;

import com.chacuio.ticketvortexapi.user.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
// revisar: La contraseña deberia estar en eeste DTO? o como se manejaria al momento de registrar un usuario? ya que el user module se relacionara con el auth
public record UserRequestDTO(
        @NotBlank(message = "The first name field is required")
        @Size(min = 1, max = 120, message = "The first name field must be between 1 and 120 characters long")
        String firstName,

        @NotBlank(message = "The last name field is required")
        @Size(min = 1, max = 120, message = "The last name field must be between 1 and 120 characters long")
        String lastName,

        @Email(message = "The email field must have a valid format")
        @NotBlank(message = "The email field is required")
        @Size(max = 255, message = "The email field cannot exceed 255 characters")
        String email,

        @NotBlank(message = "The document number field is required")
        @Size(min = 1, max = 50, message = "The document number field must be between 1 and 50 characters long")
        String documentNumber,

        @NotNull(message = "The role field is required")
        Role role
) {}
