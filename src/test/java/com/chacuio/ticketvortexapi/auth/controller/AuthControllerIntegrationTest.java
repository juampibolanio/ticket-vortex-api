package com.chacuio.ticketvortexapi.auth.controller;

import com.chacuio.ticketvortexapi.auth.dto.LoginRequestDTO;
import com.chacuio.ticketvortexapi.auth.dto.RegisterRequestDTO;
import com.chacuio.ticketvortexapi.user.model.Role;
import com.chacuio.ticketvortexapi.user.model.User;
import com.chacuio.ticketvortexapi.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Integration: Register should create user, hash password, and return JWT token")
    void register_Success() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO(
                "Martin",
                "Palermo",
                "martin@example.com",
                "99999999",
                "Password123!",
                Role.CUSTOMER
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.userData.email", is("martin@example.com")))
                .andExpect(jsonPath("$.userData.role", is("CUSTOMER")));

        User savedUser = userRepository.findByEmail("martin@example.com").orElseThrow();
        assertThat(passwordEncoder.matches("Password123!", savedUser.getPassword())).isTrue();
    }

    @Test
    @DisplayName("Integration: Register with duplicate email should return 409 Conflict")
    void register_DuplicateEmail_Conflict() throws Exception {
        User existingUser = User.builder()
                .firstName("Juan")
                .lastName("Perez")
                .email("juan@example.com")
                .documentNumber("12345678")
                .password(passwordEncoder.encode("Secret123!"))
                .role(Role.CUSTOMER)
                .isActive(true)
                .build();
        userRepository.save(existingUser);

        RegisterRequestDTO request = new RegisterRequestDTO(
                "Juan",
                "Otro",
                "juan@example.com",
                "87654321",
                "Password123!",
                Role.CUSTOMER
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Integration: Register with invalid fields should return 400 Bad Request")
    void register_InvalidFields_BadRequest() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO(
                "",
                "Lopez",
                "not-an-email",
                "12345678",
                "short",
                Role.CUSTOMER
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors", notNullValue()));
    }

    @Test
    @DisplayName("Integration: Login with valid credentials should return JWT token")
    void login_Success() throws Exception {
        User user = User.builder()
                .firstName("Román")
                .lastName("Riquelme")
                .email("roman@example.com")
                .documentNumber("10101010")
                .password(passwordEncoder.encode("Riquelme10!"))
                .role(Role.ADMIN)
                .isActive(true)
                .build();
        userRepository.save(user);

        LoginRequestDTO request = new LoginRequestDTO("roman@example.com", "Riquelme10!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()));
    }

    @Test
    @DisplayName("Integration: Login with incorrect password should return 401 Unauthorized")
    void login_WrongPassword_Unauthorized() throws Exception {
        User user = User.builder()
                .firstName("Román")
                .lastName("Riquelme")
                .email("roman@example.com")
                .documentNumber("10101010")
                .password(passwordEncoder.encode("Riquelme10!"))
                .role(Role.ADMIN)
                .isActive(true)
                .build();
        userRepository.save(user);

        LoginRequestDTO request = new LoginRequestDTO("roman@example.com", "WrongPassword!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
