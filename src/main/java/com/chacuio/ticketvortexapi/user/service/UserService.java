package com.chacuio.ticketvortexapi.user.service;

import com.chacuio.ticketvortexapi.user.dto.UserPatchRequestDTO;
import com.chacuio.ticketvortexapi.user.dto.UserRequestDTO;
import com.chacuio.ticketvortexapi.user.dto.UserResponseDTO;
import com.chacuio.ticketvortexapi.user.dto.UserSummaryDTO;
import com.chacuio.ticketvortexapi.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {
    Page<UserSummaryDTO> findAll(Pageable pageable);
    UserResponseDTO findById(UUID id);
    UserResponseDTO create(UserRequestDTO dto);
    UserResponseDTO patch(UUID id, UserPatchRequestDTO dto);
    void delete(UUID id);
    User findEntityById(UUID id);
}
