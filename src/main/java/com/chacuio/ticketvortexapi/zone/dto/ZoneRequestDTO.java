package com.chacuio.ticketvortexapi.zone.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Data required to create an event zone")
public record ZoneRequestDTO(
        @Schema(description = "Name of the zone", example = "VIP")
        @NotBlank(message = "Name is required")
        @Length(min = 1, max = 120, message = "The name field must be between 1 and 120 characters long")
        String name,

        @Schema(description = "Description of the zone", example = "VIP area with exclusive access")
        @Length(max = 250, message = "The description must have a maximum of 250 characters")
        String description,

        @Schema(
                description = "Ticket price for this zone",
                example = "25000.00"
        )
        @NotNull(message = "Price is required")
        @PositiveOrZero(message = "The price must be positive or zero")
        BigDecimal price,

        @Schema(
                description = "Maximum number of people allowed in the zone",
                example = "500"
        )
        @NotNull(message = "Capacity is required")
        @Positive(message = "The capacity must be positive")
        @Min(1)
        Integer capacity,

        @Schema(
                description = "ID of the event associated with this zone",
                example = "550e8400-e29b-41d4-a716-446655440000"
        )
        UUID eventId // check if this field is necessary
) { }
