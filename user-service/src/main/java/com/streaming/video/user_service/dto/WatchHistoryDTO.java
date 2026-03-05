package com.streaming.video.user_service.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for WatchHistory responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatchHistoryDTO {

    private Long id;
    private Long userId;
    private Long videoId;
    private LocalDateTime watchedAt;
    private Integer progressTime;
    private Boolean completed;
}
