package com.streaming.video.user_service.dto;

import lombok.*;

/**
 * DTO for authentication responses — contains JWT token and user info.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {

    private String token;
    private String tokenType = "Bearer";
    private String username;
    private String email;
    private String role;
}

