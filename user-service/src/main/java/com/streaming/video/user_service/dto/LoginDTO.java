package com.streaming.video.user_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * DTO for login requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginDTO {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}

