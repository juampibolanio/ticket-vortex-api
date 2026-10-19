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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<UserSummaryDTO> findAll(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toSummaryDto);
    }

    @Override
    public UserResponseDTO findById(UUID id) {
        return userMapper.toDto(userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id)));
    }

    @Override
    @Transactional
    public UserResponseDTO create(UserRequestDTO dto) {
        if (userRepository.existsByDocumentNumber(dto.documentNumber())) {
            throw new DataConflictException("Document number already exists");
        }
        if (userRepository.existsByEmail(dto.email())) {
            throw new DataConflictException("Email already exists");
        }

        User user = userMapper.toEntity(dto);
        user.setPassword(passwordEncoder.encode(dto.password()));
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponseDTO patch(UUID id, UserPatchRequestDTO dto) {
        User user = userRepository.findById(id)
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

        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        userRepository.delete(user);
    }

    @Override
    public User findEntityById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }
}
