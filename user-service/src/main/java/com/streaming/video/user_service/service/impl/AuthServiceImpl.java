package com.streaming.video.user_service.service.impl;

import com.streaming.video.user_service.dto.AuthResponseDTO;
import com.streaming.video.user_service.dto.LoginDTO;
import com.streaming.video.user_service.dto.RegisterDTO;
import com.streaming.video.user_service.entity.User;
import com.streaming.video.user_service.repository.UserRepository;
import com.streaming.video.user_service.security.JwtTokenProvider;
import com.streaming.video.user_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of AuthService — handles user registration and login with JWT token generation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public AuthResponseDTO register(RegisterDTO registerDTO) {
        log.info("Registering new user: {}", registerDTO.getUsername());

        // Check for duplicates
        if (userRepository.existsByUsername(registerDTO.getUsername())) {
            throw new IllegalStateException("Username already in use: " + registerDTO.getUsername());
        }
        if (userRepository.existsByEmail(registerDTO.getEmail())) {
            throw new IllegalStateException("Email already in use: " + registerDTO.getEmail());
        }

        // Create and save the user
        User user = User.builder()
                .username(registerDTO.getUsername())
                .email(registerDTO.getEmail())
                .password(passwordEncoder.encode(registerDTO.getPassword()))
                .role("ROLE_USER")
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered with id: {}", savedUser.getId());

        // Authenticate and generate token
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(registerDTO.getUsername(), registerDTO.getPassword()));

        String token = jwtTokenProvider.generateToken(authentication);

        return AuthResponseDTO.builder()
                .token(token)
                .tokenType("Bearer")
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .build();
    }

    @Override
    public AuthResponseDTO login(LoginDTO loginDTO) {
        log.info("Authenticating user: {}", loginDTO.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword()));

        String token = jwtTokenProvider.generateToken(authentication);

        User user = userRepository.findByUsername(loginDTO.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        log.info("User authenticated successfully: {}", user.getUsername());

        return AuthResponseDTO.builder()
                .token(token)
                .tokenType("Bearer")
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}

