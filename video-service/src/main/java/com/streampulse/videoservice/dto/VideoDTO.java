import com.streampulse.videoservice.entity.VideoCategory;
import com.streampulse.videoservice.entity.VideoType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data Transfer Object for Video entity")
public class VideoDTO {

    @Schema(description = "Unique identifier of the video", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    @Schema(description = "Title of the video", example = "Inception")
    private String title;

    @Schema(description = "Full description of the video content", example = "A thief who steals corporate secrets...")
    private String description;

    @Schema(description = "URL to the video thumbnail image", example = "http://example.com/thumb.jpg")
    private String thumbnailUrl;

    @Schema(description = "URL to the video trailer", example = "http://example.com/trailer.mp4")
    private String trailerUrl;

    @Positive(message = "Duration must be a positive number")
    @Schema(description = "Duration of the video in minutes", example = "148")
    private Integer duration;

    @Min(value = 1888, message = "Release year must be at least 1888")
    @Max(value = 2100, message = "Release year is not realistic")
    @Schema(description = "The year the video was released", example = "2010")
    private Integer releaseYear;

    @NotNull(message = "Video type is required")
    @Schema(description = "Type of the video (FILM or SERIE)", example = "FILM")
    private VideoType type;

    @NotNull(message = "Video category is required")
    @Schema(description = "Category of the video content", example = "SCIENCE_FICTION")
    private VideoCategory category;

    @DecimalMin(value = "0.0", message = "Rating must be at least 0.0")
    @DecimalMax(value = "10.0", message = "Rating must be at most 10.0")
    @Schema(description = "Average rating (0.0 to 10.0)", example = "8.8")
    private Double rating;

    @Schema(description = "Director of the video", example = "Christopher Nolan")
    private String director;

    @Schema(description = "Main cast members", example = "Leonardo DiCaprio, Ellen Page")
    private String cast;
}
