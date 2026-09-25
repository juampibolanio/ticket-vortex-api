package com.chacuio.ticketvortexapi.event.dto;

import com.chacuio.ticketvortexapi.zone.dto.ZoneRequestDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Data required to create an event")
public record EventRequestDTO(

        @Schema(description = "Event title", example = "John's Band - Live Concert")
        @NotBlank(message = "Title is required")
        @Length(min = 1, max = 120, message = "The title field must be between 1 and 120 characters long")
        String title,

        @Schema(description = "Detailed description of the event", example = "Live concert featuring John's Band")
        @Length(max = 500)
        String description,

        @Schema(description = "Event date and time", example = "2026-12-20T21:00:00")
        @NotNull(message = "Date is required")
        @Future
        LocalDateTime date,

        @Schema(description = "Location where the event will take place", example = "Resistencia Convention Center")
        @NotNull(message = "Location is required")
        @Length(min = 1, max = 120, message = "Location must be between 1 and 120 characters long")
        String location,

        @Schema(description = "Zones associated with the event. At least one zone is required.")
        @NotEmpty(message = "An event must have at least one zone")
        @Valid
        List<ZoneRequestDTO> zones
) { }
