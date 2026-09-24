package com.chacuio.ticketvortexapi.user.service;

import com.chacuio.ticketvortexapi.user.dto.UserRequestDTO;
import com.chacuio.ticketvortexapi.user.dto.UserResponseDTO;
import com.chacuio.ticketvortexapi.user.dto.UserSummaryDTO;
import com.chacuio.ticketvortexapi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;

    @Override
    public List<UserSummaryDTO> findAll() {
        return List.of();
    }

    @Override
    public List<UserResponseDTO> findById(UUID id) {
        return List.of();
    }

    @Override
    public UserResponseDTO create(UserRequestDTO dto) {
        return null;
    }

    @Override
    public UserResponseDTO patch(UUID id, UserRequestDTO dto) {
        return null;
    }

    @Override
    public void delete(UUID id) {

    }
}
