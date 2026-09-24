package com.chacuio.ticketvortexapi.auth.service;

import com.chacuio.ticketvortexapi.auth.dto.LoginRequestDTO;
import com.chacuio.ticketvortexapi.auth.dto.LoginResponseDTO;
import com.chacuio.ticketvortexapi.auth.dto.RegisterRequestDTO;

public interface AuthService {
    LoginResponseDTO login(LoginRequestDTO loginRequestDTO);
    RegisterRequestDTO register(RegisterRequestDTO registerRequestDTO);
}
