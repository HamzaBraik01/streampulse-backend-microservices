package com.streampulse.videoservice.dto;

import com.streampulse.videoservice.entity.VideoCategory;
import com.streampulse.videoservice.entity.VideoType;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoDTO {

    private Long id;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    private String description;

    private String thumbnailUrl;

    private String trailerUrl;

    @Positive(message = "Duration must be a positive number")
    private Integer duration;

    @Min(value = 1888, message = "Release year must be at least 1888")
    @Max(value = 2100, message = "Release year is not realistic")
    private Integer releaseYear;

    @NotNull(message = "Video type is required")
    private VideoType type;

    @NotNull(message = "Video category is required")
    private VideoCategory category;

    @DecimalMin(value = "0.0", message = "Rating must be at least 0.0")
    @DecimalMax(value = "10.0", message = "Rating must be at most 10.0")
    private Double rating;

    private String director;

    private String cast;
}
