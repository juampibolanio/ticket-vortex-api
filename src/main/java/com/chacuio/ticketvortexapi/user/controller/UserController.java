package com.chacuio.ticketvortexapi.user.controller;

import com.chacuio.ticketvortexapi.user.dto.UserPatchRequestDTO;
import com.chacuio.ticketvortexapi.user.dto.UserRequestDTO;
import com.chacuio.ticketvortexapi.user.dto.UserResponseDTO;
import com.chacuio.ticketvortexapi.user.dto.UserSummaryDTO;
import com.chacuio.ticketvortexapi.user.service.UserService;
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
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Endpoints for managing users")
public class UserController {
    private final UserService userService;

    @Operation(summary = "Get all users", description = "Returns a list with all registered users.")
    @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    @GetMapping
    public ResponseEntity<List<UserSummaryDTO>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @Operation(summary = "Get user by ID", description = "Returns the complete information of a user.")
    @ApiResponse(responseCode = "200", description = "User found successfully.")
    @ApiResponse(responseCode = "404", description = "User not found")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> findById(
            @Parameter(description = "Unique identifier of the user", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id)
    {
        return  ResponseEntity.ok(userService.findById(id));
    }

    @Operation(summary = "Create a new user", description = "Creates a user")
    @ApiResponse(responseCode = "201", description = "User created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "409", description = "The request data conflicts with other data.")
    @PostMapping
    public ResponseEntity<UserResponseDTO> create (
        @Valid @RequestBody UserRequestDTO dto
    )
    {
        UserResponseDTO responseDTO = userService.create(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseDTO);
    }

    @Operation(summary = "Patch a user", description = "Updates the provided user fields.")
    @ApiResponse(responseCode = "200", description = "User updated successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "409", description = "The request data conflicts with other data.")
    @PatchMapping("/{id}")
    public ResponseEntity<UserResponseDTO> patch(
        @Parameter(description = "Unique identifier of the user", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable UUID id,
        @Valid @RequestBody UserPatchRequestDTO dto
    )
    {
        UserResponseDTO responseDTO = userService.patch(id, dto);
        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "Delete a user by ID", description = "Deletes the user with the specified ID.")
    @ApiResponse(responseCode = "204", description = "User deleted successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(
        @Parameter(description = "Unique identifier of the user", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable UUID id
    )
    {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
