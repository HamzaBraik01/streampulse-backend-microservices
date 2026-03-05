package com.streaming.video.user_service.service;

import com.streaming.video.user_service.dto.AuthResponseDTO;
import com.streaming.video.user_service.dto.LoginDTO;
import com.streaming.video.user_service.dto.RegisterDTO;

/**
 * Service interface for authentication operations.
 */
public interface AuthService {

    AuthResponseDTO register(RegisterDTO registerDTO);

    AuthResponseDTO login(LoginDTO loginDTO);
}

