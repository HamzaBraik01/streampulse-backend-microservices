package com.streaming.video.user_service.controller;

import com.streaming.video.user_service.dto.AuthResponseDTO;
import com.streaming.video.user_service.dto.LoginDTO;
import com.streaming.video.user_service.dto.RegisterDTO;
import com.streaming.video.user_service.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Authentication — handles registration and login.
 * Endpoints are publicly accessible (no JWT required).
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration and login with JWT token generation")
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/register — Register a new user and receive a JWT token.
     */
    @PostMapping("/register")
    @Operation(summary = "Register", description = "Create a new user account and receive a JWT token")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        AuthResponseDTO response = authService.register(registerDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/auth/login — Authenticate and receive a JWT token.
     */
    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate with username and password to receive a JWT token")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        AuthResponseDTO response = authService.login(loginDTO);
        return ResponseEntity.ok(response);
    }
}

