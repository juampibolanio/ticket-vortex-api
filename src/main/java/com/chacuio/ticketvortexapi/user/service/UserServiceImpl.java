package com.chacuio.ticketvortexapi.user.service;

import com.chacuio.ticketvortexapi.common.exceptions.DataConflictException;
import com.chacuio.ticketvortexapi.common.exceptions.ResourceNotFoundException;
import com.chacuio.ticketvortexapi.user.dto.UserPatchRequestDTO;
import com.chacuio.ticketvortexapi.user.dto.UserRequestDTO;
import com.chacuio.ticketvortexapi.user.dto.UserResponseDTO;
import com.chacuio.ticketvortexapi.user.dto.UserSummaryDTO;
import com.chacuio.ticketvortexapi.user.mapper.UserMapper;
import com.chacuio.ticketvortexapi.user.model.User;
import com.chacuio.ticketvortexapi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserSummaryDTO> findAll() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(userMapper::toSummaryDto)
                .toList();
    }

    @Override
    public UserResponseDTO findById(UUID id) {
        return userMapper.toDto(userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id)));
    }

    @Override
    public UserResponseDTO create(UserRequestDTO dto) {
        if (userRepository.existsByDocumentNumber(dto.documentNumber())) {
            throw new DataConflictException("Document number already exists");
        }
        if (userRepository.existsByEmail(dto.email())) {
            throw new DataConflictException("Email already exists");
        }

        return  userMapper.toDto(userRepository.save(userMapper.toEntity(dto)));
    }

    @Override
    public UserResponseDTO patch(UUID id, UserPatchRequestDTO dto) {
        User user =  userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (dto.email() != null && !dto.email().equals(user.getEmail())
                && userRepository.existsByEmailAndIdNot(dto.email(), id)) {
            throw new DataConflictException("Email already exists");
        }

        if (dto.documentNumber() != null && !dto.documentNumber().equals(user.getDocumentNumber())
                && userRepository.existsByDocumentNumberAndIdNot(dto.documentNumber(), id)) {
            throw new DataConflictException("Document number already exists");
        }

        if (dto.firstName() != null) user.setFirstName(dto.firstName());
        if (dto.lastName() != null) user.setLastName(dto.lastName());
        if (dto.email() != null) user.setEmail(dto.email());
        if (dto.documentNumber() != null) user.setDocumentNumber(dto.documentNumber());
        if (dto.role() != null) user.setRole(dto.role());

        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public void delete(UUID id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        }
        else {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
    }
}
