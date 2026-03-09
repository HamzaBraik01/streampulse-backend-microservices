package com.streaming.video.user_service.service;

import com.streaming.video.user_service.dto.AuthResponseDTO;
import com.streaming.video.user_service.dto.LoginDTO;
import com.streaming.video.user_service.dto.RegisterDTO;
import com.streaming.video.user_service.entity.User;
import com.streaming.video.user_service.repository.UserRepository;
import com.streaming.video.user_service.security.JwtTokenProvider;
import com.streaming.video.user_service.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthServiceImpl — tests registration and login logic.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterDTO registerDTO;
    private LoginDTO loginDTO;
    private User savedUser;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        registerDTO = RegisterDTO.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();

        loginDTO = LoginDTO.builder()
                .username("testuser")
                .password("password123")
                .build();

        savedUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("bcrypt_hashed")
                .role("ROLE_USER")
                .build();

        authentication = mock(Authentication.class);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // register
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("register - should register user and return JWT token")
    void register_ShouldRegisterAndReturnToken() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("bcrypt_hashed");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("jwt-token-123");

        AuthResponseDTO result = authService.register(registerDTO);

        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("jwt-token-123");
        assertThat(result.getTokenType()).isEqualTo("Bearer");
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getRole()).isEqualTo("ROLE_USER");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register - should throw when username already exists")
    void register_ShouldThrowWhenUsernameExists() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerDTO))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Username already in use");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("register - should throw when email already exists")
    void register_ShouldThrowWhenEmailExists() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerDTO))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Email already in use");

        verify(userRepository, never()).save(any());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // login
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("login - should authenticate and return JWT token")
    void login_ShouldAuthenticateAndReturnToken() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("jwt-token-456");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(savedUser));

        AuthResponseDTO result = authService.login(loginDTO);

        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("jwt-token-456");
        assertThat(result.getTokenType()).isEqualTo("Bearer");
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getRole()).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("login - should throw when credentials are invalid")
    void login_ShouldThrowWhenCredentialsInvalid() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(loginDTO))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("login - should throw when user not found after authentication")
    void login_ShouldThrowWhenUserNotFoundAfterAuth() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("jwt-token");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginDTO))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("User not found");
    }
}
