package com.chacuio.ticketvortexapi.user.service;

import com.chacuio.ticketvortexapi.user.dto.UserPatchRequestDTO;
import com.chacuio.ticketvortexapi.user.dto.UserRequestDTO;
import com.chacuio.ticketvortexapi.user.dto.UserResponseDTO;
import com.chacuio.ticketvortexapi.user.dto.UserSummaryDTO;

import java.util.List;
import java.util.UUID;

public interface UserService {
    List<UserSummaryDTO> findAll();
    UserResponseDTO findById(UUID id);
    UserResponseDTO create(UserRequestDTO dto);
    UserResponseDTO patch(UUID id, UserPatchRequestDTO dto);
    void delete(UUID id);
}
