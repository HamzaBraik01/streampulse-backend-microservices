package com.streaming.video.security_service.controller;

import com.streaming.video.security_service.dto.AuthRequest;
import com.streaming.video.security_service.dto.AuthResponse;
import com.streaming.video.security_service.entity.User;
import com.streaming.video.security_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        authService.register(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        String token = authService.login(authRequest.getUsername(), authRequest.getPassword());
        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .username(authRequest.getUsername())
                .build());
    }

    @GetMapping("/validate")
    public ResponseEntity<String> validate(@RequestParam("token") String token) {
        // Simple validation for now, could be expanded
        return ResponseEntity.ok("Token is valid");
    }
}
