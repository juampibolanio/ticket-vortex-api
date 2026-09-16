package com.chacuio.ticketvortexapi.user.mapper;

import com.chacuio.ticketvortexapi.user.dto.UserRequestDTO;
import com.chacuio.ticketvortexapi.user.dto.UserResponseDTO;
import com.chacuio.ticketvortexapi.user.dto.UserSummaryDTO;
import com.chacuio.ticketvortexapi.user.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserResponseDTO toDto(User user) {
        return new UserResponseDTO(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getDocumentNumber(),
            user.getRole(),
            user.isActive(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }

    public User toEntity(UserRequestDTO dto) {
        return User.builder()
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .email(dto.email())
                .documentNumber(dto.documentNumber())
                .role(dto.role())
                .password(dto.password())
                .build();
    }

    public UserSummaryDTO toSummaryDto(User user) {
        return new UserSummaryDTO(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail()
        );
    }
}
