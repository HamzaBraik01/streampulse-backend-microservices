package com.streaming.video.user_service.dto;

import lombok.*;

/**
 * DTO for viewing statistics per user.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViewingStatsDTO {

    private Long userId;
    private long totalVideosWatched;
    private long completedVideos;
    private double completionRate;
    private long totalWatchTimeMinutes;
}
