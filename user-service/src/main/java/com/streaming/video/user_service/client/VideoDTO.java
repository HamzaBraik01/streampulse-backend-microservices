package com.streaming.video.user_service.client;

import lombok.*;

/**
 * Lightweight DTO to deserialize video-service responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoDTO {

    private Long id;
    private String title;
    private String description;
    private String thumbnailUrl;
    private String trailerUrl;
    private Integer duration;
    private Integer releaseYear;
    private String type;
    private String category;
    private Double rating;
    private String director;
    private String cast;
}
