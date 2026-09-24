package com.chacuio.ticketvortexapi.user.service;

import com.chacuio.ticketvortexapi.user.dto.UserRequestDTO;
import com.chacuio.ticketvortexapi.user.dto.UserResponseDTO;
import com.chacuio.ticketvortexapi.user.dto.UserSummaryDTO;

import java.util.List;
import java.util.UUID;

public interface UserService {
    List<UserSummaryDTO> findAll();
    List<UserResponseDTO> findById(UUID id);
    UserResponseDTO create(UserRequestDTO dto);
    UserResponseDTO patch(UUID id, UserRequestDTO dto);
    void delete(UUID id);
}
