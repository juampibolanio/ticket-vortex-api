package com.chacuio.ticketvortexapi.auth.service;

import com.chacuio.ticketvortexapi.auth.dto.LoginRequestDTO;
import com.chacuio.ticketvortexapi.auth.dto.RegisterResponseDTO;
import com.chacuio.ticketvortexapi.auth.dto.TokenResponseDTO;
import com.chacuio.ticketvortexapi.auth.dto.RegisterRequestDTO;
import com.chacuio.ticketvortexapi.security.jwt.JwtService;
import com.chacuio.ticketvortexapi.user.dto.UserRequestDTO;
import com.chacuio.ticketvortexapi.user.dto.UserResponseDTO;
import com.chacuio.ticketvortexapi.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    @Override
    public TokenResponseDTO login(LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        UserDetails user = userDetailsService.loadUserByUsername(request.email());
        String token = jwtService.generateToken(user);

        return new TokenResponseDTO(token);
    }

    @Override
    public RegisterResponseDTO register(RegisterRequestDTO request) {
        UserRequestDTO userDto = new UserRequestDTO(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.documentNumber(),
                passwordEncoder.encode(request.password()),
                request.role()
        );

        UserResponseDTO createdUser = userService.create(userDto);

        UserDetails userDetails = userDetailsService.loadUserByUsername(createdUser.email());
        String token = jwtService.generateToken(userDetails);

        return new RegisterResponseDTO(createdUser, token);
    }
}
