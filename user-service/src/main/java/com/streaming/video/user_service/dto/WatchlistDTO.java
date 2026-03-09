package com.streaming.video.user_service.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for Watchlist entries.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatchlistDTO {

    private Long id;
    private Long userId;
    private Long videoId;
    private LocalDateTime addedAt;
}
