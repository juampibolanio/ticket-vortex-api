package com.chacuio.ticketvortexapi.zone.dto;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.util.UUID;

public record ZoneRequestDTO(
        @NotBlank(message = "Name is required")
        @Length(min = 1, max = 120, message = "The name field must be between 1 and 120 characters long")
        String name,

        @Length(max = 250, message = "The description must have a maximum of 250 characters")
        String description,

        @NotNull(message = "Price is required")
        @PositiveOrZero(message = "The price must be positive or zero")
        BigDecimal price,

        @NotNull(message = "Capacity is required")
        @Positive(message = "The capacity must be positive")
        @Min(1)
        Integer capacity,

        @NotNull(message = "Event ID is required")
        UUID eventId
) { }
