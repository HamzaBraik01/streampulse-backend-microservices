package com.streaming.video.user_service.dto;

import lombok.*;

/**
 * DTO for User responses — password is never exposed.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long id;
    private String username;
    private String email;
}
