package com.chacuio.ticketvortexapi.event.dto;

import com.chacuio.ticketvortexapi.zone.dto.ZoneRequestDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;
import java.util.List;

public record EventRequestDTO(
        @NotBlank(message = "Title is required")
        @Length(min = 1, max = 120, message = "The title field must be between 1 and 120 characters long")
        String title,

        @Length(max = 500)
        String description,

        @NotNull(message = "Date is required")
        @Future
        LocalDateTime date,

        @NotNull(message = "Location is required")
        @Length(min = 1, max = 120, message = "Location must be between 1 and 120 characters long")
        String location,

        @NotEmpty(message = "An event must have at least one zone")
        @Valid
        List<ZoneRequestDTO> zones
) { }
