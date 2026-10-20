package com.chacuio.ticketvortexapi.user.controller;

import com.chacuio.ticketvortexapi.user.dto.UserPatchRequestDTO;
import com.chacuio.ticketvortexapi.user.dto.UserRequestDTO;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerIntegrationTest {

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
    @DisplayName("Integration: Should return paginated users for ADMIN")
    @WithMockUser(roles = "ADMIN")
    void findAll_AsAdmin_Success() throws Exception {
        userRepository.save(createUser("Carlos", "Gomez", "carlos@example.com", "11111111"));
        userRepository.save(createUser("Ana", "Alvarez", "ana@example.com", "22222222"));

        mockMvc.perform(get("/api/v1/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(2)));
    }

    @Test
    @DisplayName("Integration: Should return 403 Forbidden when CUSTOMER tries to list all users")
    @WithMockUser(roles = "CUSTOMER")
    void findAll_AsCustomer_Forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Integration: Should return user by ID for authenticated user")
    @WithMockUser(roles = "CUSTOMER")
    void findById_Success() throws Exception {
        User savedUser = userRepository.save(createUser("Maria", "Lopez", "maria@example.com", "33333333"));

        mockMvc.perform(get("/api/v1/users/{id}", savedUser.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedUser.getId().toString())))
                .andExpect(jsonPath("$.email", is("maria@example.com")));
    }

    @Test
    @DisplayName("Integration: Should allow ADMIN to create user with encrypted password")
    @WithMockUser(roles = "ADMIN")
    void create_AsAdmin_SuccessAndEncryptsPassword() throws Exception {
        UserRequestDTO request = new UserRequestDTO(
                "Lucas",
                "Torres",
                "lucas@example.com",
                "44444444",
                "RawPassword123!",
                Role.CUSTOMER
        );

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email", is("lucas@example.com")));

        // Validamos en base de datos que la contraseña no esté en texto plano
        User userInDb = userRepository.findByEmail("lucas@example.com").orElseThrow();
        assertThat(userInDb.getPassword()).isNotEqualTo("RawPassword123!");
        assertThat(passwordEncoder.matches("RawPassword123!", userInDb.getPassword())).isTrue();
    }

    @Test
    @DisplayName("Integration: Should patch user fields successfully")
    @WithMockUser(roles = "CUSTOMER")
    void patch_Success() throws Exception {
        User savedUser = userRepository.save(createUser("Pedro", "Rios", "pedro@example.com", "55555555"));
        UserPatchRequestDTO patchDto = new UserPatchRequestDTO("Pedro Antonio", null, null, null);

        mockMvc.perform(patch("/api/v1/users/{id}", savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("Pedro Antonio")));
    }

    @Test
    @DisplayName("Integration: Should delete user as ADMIN")
    @WithMockUser(roles = "ADMIN")
    void delete_AsAdmin_Success() throws Exception {
        User savedUser = userRepository.save(createUser("Eliminar", "Usuario", "delete@example.com", "66666666"));

        mockMvc.perform(delete("/api/v1/users/{id}", savedUser.getId()))
                .andExpect(status().isNoContent());

        assertThat(userRepository.findById(savedUser.getId())).isEmpty();
    }

    private User createUser(String firstName, String lastName, String email, String documentNumber) {
        return User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .documentNumber(documentNumber)
                .password(passwordEncoder.encode("Secret123!"))
                .role(Role.CUSTOMER)
                .isActive(true)
                .build();
    }
}
