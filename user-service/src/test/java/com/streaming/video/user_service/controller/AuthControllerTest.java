package com.streaming.video.user_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streaming.video.user_service.config.SecurityConfig;
import com.streaming.video.user_service.dto.AuthResponseDTO;
import com.streaming.video.user_service.dto.LoginDTO;
import com.streaming.video.user_service.dto.RegisterDTO;
import com.streaming.video.user_service.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AuthController — tests authentication endpoints using MockMvc.
 */
@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    // ═══════════════════════════════════════════════════════════════════════════
    // POST /api/auth/register
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("POST /api/auth/register - should register user with 201")
    void register_ShouldReturnCreated() throws Exception {
        RegisterDTO registerDTO = RegisterDTO.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();

        AuthResponseDTO response = AuthResponseDTO.builder()
                .token("jwt-token-123")
                .tokenType("Bearer")
                .username("testuser")
                .email("test@example.com")
                .role("ROLE_USER")
                .build();

        when(authService.register(any(RegisterDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token-123"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"));
    }

    @Test
    @DisplayName("POST /api/auth/register - should return 400 when username is blank")
    void register_ShouldReturn400WhenUsernameBlank() throws Exception {
        RegisterDTO invalid = RegisterDTO.builder()
                .username("")
                .email("test@example.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/register - should return 400 when email is invalid")
    void register_ShouldReturn400WhenEmailInvalid() throws Exception {
        RegisterDTO invalid = RegisterDTO.builder()
                .username("testuser")
                .email("not-an-email")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/register - should return 400 when password too short")
    void register_ShouldReturn400WhenPasswordTooShort() throws Exception {
        RegisterDTO invalid = RegisterDTO.builder()
                .username("testuser")
                .email("test@example.com")
                .password("123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/register - should return 400 when username already exists")
    void register_ShouldReturn400WhenUsernameExists() throws Exception {
        RegisterDTO registerDTO = RegisterDTO.builder()
                .username("existinguser")
                .email("test@example.com")
                .password("password123")
                .build();

        when(authService.register(any(RegisterDTO.class)))
                .thenThrow(new IllegalStateException("Username already in use"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isBadRequest());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // POST /api/auth/login
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("POST /api/auth/login - should login and return JWT token with 200")
    void login_ShouldReturnToken() throws Exception {
        LoginDTO loginDTO = LoginDTO.builder()
                .username("testuser")
                .password("password123")
                .build();

        AuthResponseDTO response = AuthResponseDTO.builder()
                .token("jwt-token-456")
                .tokenType("Bearer")
                .username("testuser")
                .email("test@example.com")
                .role("ROLE_USER")
                .build();

        when(authService.login(any(LoginDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-456"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @DisplayName("POST /api/auth/login - should return 400 when username is blank")
    void login_ShouldReturn400WhenUsernameBlank() throws Exception {
        LoginDTO invalid = LoginDTO.builder()
                .username("")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login - should return 400 when password is blank")
    void login_ShouldReturn400WhenPasswordBlank() throws Exception {
        LoginDTO invalid = LoginDTO.builder()
                .username("testuser")
                .password("")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }
}
