package com.chacuio.ticketvortexapi.user.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserPatchRequestDTO(
        @Pattern(regexp = "(?s).*\\S.*", message = "The first name field must not be blank")
        @Size(min = 1, max = 120, message = "The first name field must be between 1 and 120 characters long")
        String firstName,

        @Pattern(regexp = "(?s).*\\S.*", message = "The last name field must not be blank")
        @Size(min = 1, max = 120, message = "The last name field must be between 1 and 120 characters long")
        String lastName,

        @Pattern(regexp = "(?s).*\\S.*", message = "The email field must not be blank")
        @Email(message = "The email field must have a valid format")
        @Size(max = 255, message = "The email field cannot exceed 255 characters")
        String email,

        @Pattern(regexp = "(?s).*\\S.*", message = "The document number field must not be blank")
        @Size(min = 1, max = 50, message = "The document number field must be between 1 and 50 characters long")
        String documentNumber
) {
    @AssertTrue(message = "At least one field must be provided")
    public boolean hasAtLeastOneField() {
        return firstName != null || lastName != null || email != null || documentNumber != null;
    }
}
