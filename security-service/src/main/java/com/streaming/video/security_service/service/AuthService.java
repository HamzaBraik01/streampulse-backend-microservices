package com.streaming.video.security_service.service;

import com.streaming.video.security_service.entity.User;
import com.streaming.video.security_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public void register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    public String login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        
        if (passwordEncoder.matches(password, user.getPassword())) {
            return jwtService.generateToken(username);
        } else {
            throw new RuntimeException("Invalid credentials");
        }
    }
}
