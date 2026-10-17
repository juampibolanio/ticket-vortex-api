package com.chacuio.ticketvortexapi.zone.controller;

import com.chacuio.ticketvortexapi.zone.dto.ZoneRequestDTO;
import com.chacuio.ticketvortexapi.zone.dto.ZoneResponseDTO;
import com.chacuio.ticketvortexapi.zone.dto.ZoneSummaryDTO;
import com.chacuio.ticketvortexapi.zone.service.ZoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/zones")
@RequiredArgsConstructor
@Tag(name = "Zones", description = "Endpoints for managing event zones")
@SecurityRequirement(name = "Bearer Authentication")
public class ZoneController {

    private final ZoneService zoneService;

    @Operation(summary = "Get zone by ID", description = "Returns full information of a zone including associated event summary")
    @ApiResponse(responseCode = "200", description = "Zone retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized: Missing or invalid JWT")
    @ApiResponse(responseCode = "404", description = "Zone not found")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @GetMapping("/{id}")
    public ResponseEntity<ZoneResponseDTO> findById(
            @Parameter(description = "Unique identifier of the zone", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id) {
        return ResponseEntity.ok(zoneService.findByIdWithEvent(id));
    }

    @Operation(summary = "Get zones by event ID", description = "Returns all zones associated with a specific event")
    @ApiResponse(responseCode = "200", description = "Zones retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized: Missing or invalid JWT")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @GetMapping("/events/{eventId}")
    public ResponseEntity<List<ZoneSummaryDTO>> findAllByEventId(
            @Parameter(description = "Unique identifier of the event", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID eventId) {
        return ResponseEntity.ok(zoneService.findAllByEventId(eventId));
    }

    @Operation(summary = "Add zone to an event", description = "Creates and attaches a new zone to an existing event")
    @ApiResponse(responseCode = "201", description = "Zone created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid zone data")
    @ApiResponse(responseCode = "401", description = "Unauthorized: Missing or invalid JWT")
    @ApiResponse(responseCode = "403", description = "Forbidden: Requires ADMIN role")
    @ApiResponse(responseCode = "404", description = "Event not found")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/events/{eventId}")
    public ResponseEntity<ZoneSummaryDTO> create(
            @Parameter(description = "Unique identifier of the event", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID eventId,
            @Valid @RequestBody ZoneRequestDTO dto) {
        ZoneSummaryDTO createdZone = zoneService.create(eventId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdZone);
    }
}
