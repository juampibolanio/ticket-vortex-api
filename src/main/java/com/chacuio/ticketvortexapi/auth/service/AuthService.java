package com.chacuio.ticketvortexapi.auth.service;

import com.chacuio.ticketvortexapi.auth.dto.LoginRequestDTO;
import com.chacuio.ticketvortexapi.auth.dto.RegisterResponseDTO;
import com.chacuio.ticketvortexapi.auth.dto.TokenResponseDTO;
import com.chacuio.ticketvortexapi.auth.dto.RegisterRequestDTO;

public interface AuthService {
    TokenResponseDTO login(LoginRequestDTO loginRequestDTO);
    RegisterResponseDTO register(RegisterRequestDTO registerRequestDTO);
}
