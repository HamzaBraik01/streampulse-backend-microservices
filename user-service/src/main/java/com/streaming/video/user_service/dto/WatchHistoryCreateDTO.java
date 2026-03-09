package com.streaming.video.user_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO for creating a watch history entry.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatchHistoryCreateDTO {

    @NotNull(message = "Video ID is required")
    private Long videoId;

    private Integer progressTime;

    private Boolean completed;
}
