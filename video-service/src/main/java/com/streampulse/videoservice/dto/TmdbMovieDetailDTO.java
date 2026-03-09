package com.streampulse.videoservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for TMDb movie detail response (includes credits).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbMovieDetailDTO {

    private Long id;

    private String title;

    private String overview;

    @JsonProperty("poster_path")
    private String posterPath;

    @JsonProperty("release_date")
    private String releaseDate;

    @JsonProperty("vote_average")
    private Double voteAverage;

    private Integer runtime;

    @JsonProperty("genres")
    private List<Genre> genres;

    private Credits credits;

    private Videos videos;

    /**
     * Constructs a full poster URL from the relative path.
     */
    public String getFullPosterUrl() {
        if (posterPath == null || posterPath.isEmpty()) {
            return null;
        }
        return "https://image.tmdb.org/t/p/w500" + posterPath;
    }

    /**
     * Extracts the release year from the release date string.
     */
    public Integer getReleaseYear() {
        if (releaseDate != null && releaseDate.length() >= 4) {
            try {
                return Integer.parseInt(releaseDate.substring(0, 4));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Gets the director name from the credits crew list.
     */
    public String getDirector() {
        if (credits != null && credits.getCrew() != null) {
            return credits.getCrew().stream()
                    .filter(c -> "Director".equals(c.getJob()))
                    .map(CrewMember::getName)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    /**
     * Gets the top cast members (up to 5) as a comma-separated string.
     */
    public String getCastString() {
        if (credits != null && credits.getCast() != null) {
            return credits.getCast().stream()
                    .limit(5)
                    .map(CastMember::getName)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse(null);
        }
        return null;
    }

    /**
     * Gets the first YouTube trailer URL if available.
     */
    public String getTrailerUrl() {
        if (videos != null && videos.getResults() != null) {
            return videos.getResults().stream()
                    .filter(v -> "YouTube".equals(v.getSite()) && "Trailer".equals(v.getType()))
                    .map(v -> "https://www.youtube.com/embed/" + v.getKey())
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Genre {
        private Integer id;
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Credits {
        private List<CastMember> cast;
        private List<CrewMember> crew;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CastMember {
        private String name;
        private String character;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CrewMember {
        private String name;
        private String job;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Videos {
        private List<VideoResult> results;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VideoResult {
        private String key;
        private String site;
        private String type;
    }
}
