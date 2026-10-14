package com.chacuio.ticketvortexapi.auth.controller;

import com.chacuio.ticketvortexapi.auth.dto.LoginRequestDTO;
import com.chacuio.ticketvortexapi.auth.dto.RegisterRequestDTO;
import com.chacuio.ticketvortexapi.auth.dto.RegisterResponseDTO;
import com.chacuio.ticketvortexapi.auth.dto.TokenResponseDTO;
import com.chacuio.ticketvortexapi.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration and authentication")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "User login", description = "Authenticates a user with their email and password, returning a JWT token for subsequent requests.")
    @ApiResponse(responseCode = "200", description = "Successfully authenticated")
    @ApiResponse(responseCode = "400", description = "Invalid request data (e.g., malformed email or missing password)")
    @ApiResponse(responseCode = "401", description = "Bad credentials (unauthorized)")
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO dto
    ) {
        return ResponseEntity.ok(authService.login(dto));
    }

    @Operation(summary = "Register a new user", description = "Creates a new user account in the system and automatically logs them in, returning their data and a JWT token.")
    @ApiResponse(responseCode = "201", description = "User registered successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data (e.g., missing required fields or validation errors)")
    @ApiResponse(responseCode = "409", description = "The request data conflicts with existing data (e.g., email or document number already in use)")
    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(
            @Valid @RequestBody RegisterRequestDTO dto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.register(dto));
    }
}