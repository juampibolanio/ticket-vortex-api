package com.chacuio.ticketvortexapi.user.service;

import com.chacuio.ticketvortexapi.common.exceptions.DataConflictException;
import com.chacuio.ticketvortexapi.common.exceptions.ResourceNotFoundException;
import com.chacuio.ticketvortexapi.user.dto.UserPatchRequestDTO;
import com.chacuio.ticketvortexapi.user.dto.UserResponseDTO;
import com.chacuio.ticketvortexapi.user.mapper.UserMapper;
import com.chacuio.ticketvortexapi.user.model.Role;
import com.chacuio.ticketvortexapi.user.model.User;
import com.chacuio.ticketvortexapi.user.repository.UserRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void patch_UpdatesOnlyProvidedFieldsAndSavesExistingUser() {
        UUID userId = UUID.randomUUID();
        User user = user(userId);
        UserPatchRequestDTO request = new UserPatchRequestDTO("Updated", null, null, null);
        UserResponseDTO expected = mock(UserResponseDTO.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(expected);

        UserResponseDTO result = userService.patch(userId, request);

        assertSame(expected, result);
        assertEquals("Updated", user.getFirstName());
        assertEquals("Customer", user.getLastName());
        assertEquals("customer@example.com", user.getEmail());
        assertEquals("12345", user.getDocumentNumber());
        verify(userRepository).save(user);
        verify(userRepository, never()).existsByEmailAndIdNot(anyString(), any());
        verify(userRepository, never()).existsByDocumentNumberAndIdNot(anyString(), any());
    }

    @Test
    void patch_RejectsEmailUsedByAnotherUser() {
        UUID userId = UUID.randomUUID();
        User user = user(userId);
        UserPatchRequestDTO request = new UserPatchRequestDTO(null, null, "taken@example.com", null);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndIdNot(request.email(), userId)).thenReturn(true);

        assertThrows(DataConflictException.class, () -> userService.patch(userId, request));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void patch_AllowsUserToKeepTheirExistingUniqueValues() {
        UUID userId = UUID.randomUUID();
        User user = user(userId);
        UserPatchRequestDTO request = new UserPatchRequestDTO(
                null, null, user.getEmail(), user.getDocumentNumber());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(mock(UserResponseDTO.class));

        userService.patch(userId, request);

        verify(userRepository, never()).existsByEmailAndIdNot(anyString(), any());
        verify(userRepository, never()).existsByDocumentNumberAndIdNot(anyString(), any());
    }

    @Test
    void patch_ThrowsWhenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.patch(userId, new UserPatchRequestDTO("Updated", null, null, null))
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void patchValidation_AllowsOmittedFieldsButRejectsBlankAndEmptyRequests() {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.getValidator();
            UserPatchRequestDTO emptyRequest = new UserPatchRequestDTO(null, null, null, null);
            UserPatchRequestDTO omittedFields = new UserPatchRequestDTO("Valid name", null, null, null);
            UserPatchRequestDTO blankName = new UserPatchRequestDTO("   ", null, null, null);

            Set<String> emptyViolations = validator.validate(emptyRequest).stream()
                    .map(violation -> violation.getMessage())
                    .collect(Collectors.toSet());

            assertTrue(emptyViolations.contains("At least one field must be provided"));
            assertTrue(validator.validate(omittedFields).isEmpty());
            assertFalse(validator.validate(blankName).isEmpty());
        }
    }

    private static User user(UUID id) {
        return User.builder()
                .id(id)
                .firstName("Existing")
                .lastName("Customer")
                .email("customer@example.com")
                .documentNumber("12345")
                .password("encoded-password")
                .role(Role.CUSTOMER)
                .build();
    }
}
