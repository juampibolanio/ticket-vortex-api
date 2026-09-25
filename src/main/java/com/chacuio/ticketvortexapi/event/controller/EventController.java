package com.chacuio.ticketvortexapi.event.controller;

import com.chacuio.ticketvortexapi.event.dto.EventRequestDTO;
import com.chacuio.ticketvortexapi.event.dto.EventResponseDTO;
import com.chacuio.ticketvortexapi.event.dto.EventSummaryDTO;
import com.chacuio.ticketvortexapi.event.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Tag(name = "Events", description = "Endpoints for managing events")
public class EventController {
    private final EventService eventService;

    @Operation(summary = "Get all events", description = "Returns a list with all registered events")
    @ApiResponse(responseCode = "200", description = "Events retrieved successfully")
    @GetMapping
    public ResponseEntity<List<EventSummaryDTO>> findAll() {
        return ResponseEntity.ok(eventService.findAll());
    }

    @Operation(summary = "Get event by ID", description = "Returns the complete information of an event, including its zones.")
    @ApiResponse(responseCode = "200", description = "Event found successfully")
    @ApiResponse(responseCode = "404", description = "Event not found")
    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDTO> findOneById(
            @Parameter(description = "Unique identifier of the event", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id)
    {
        return ResponseEntity.ok(eventService.findOneById(id));
    }

    @Operation(summary = "Create a new event", description = "Creates an event and its associated zones")
    @ApiResponse(responseCode = "201", description = "Event created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @PostMapping
    public ResponseEntity<EventResponseDTO> create(
        @Valid @RequestBody EventRequestDTO dto
    ) {
        EventResponseDTO responseDTO = eventService.create(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseDTO);
    }
}
