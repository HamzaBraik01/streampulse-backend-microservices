package com.streampulse.videoservice.mapper;

import com.streampulse.videoservice.dto.VideoDTO;
import com.streampulse.videoservice.entity.Video;
import com.streampulse.videoservice.entity.VideoCategory;
import com.streampulse.videoservice.entity.VideoType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for VideoMapper — verifies MapStruct-generated entity ↔ DTO conversions.
 */
@SpringBootTest
@ActiveProfiles("test")
class VideoMapperTest {

    @Autowired
    private VideoMapper videoMapper;

    @Test
    @DisplayName("toDTO - should map all fields from entity to DTO")
    void toDTO_ShouldMapAllFields() {
        Video entity = Video.builder()
                .id(1L)
                .title("Inception")
                .description("Dream within a dream")
                .thumbnailUrl("https://example.com/thumb.jpg")
                .trailerUrl("https://youtube.com/embed/abc")
                .duration(148)
                .releaseYear(2010)
                .type(VideoType.FILM)
                .category(VideoCategory.SCIENCE_FICTION)
                .rating(8.8)
                .director("Christopher Nolan")
                .cast("Leonardo DiCaprio, Tom Hardy")
                .build();

        VideoDTO dto = videoMapper.toDTO(entity);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getTitle()).isEqualTo("Inception");
        assertThat(dto.getDescription()).isEqualTo("Dream within a dream");
        assertThat(dto.getThumbnailUrl()).isEqualTo("https://example.com/thumb.jpg");
        assertThat(dto.getTrailerUrl()).isEqualTo("https://youtube.com/embed/abc");
        assertThat(dto.getDuration()).isEqualTo(148);
        assertThat(dto.getReleaseYear()).isEqualTo(2010);
        assertThat(dto.getType()).isEqualTo(VideoType.FILM);
        assertThat(dto.getCategory()).isEqualTo(VideoCategory.SCIENCE_FICTION);
        assertThat(dto.getRating()).isEqualTo(8.8);
        assertThat(dto.getDirector()).isEqualTo("Christopher Nolan");
        assertThat(dto.getCast()).isEqualTo("Leonardo DiCaprio, Tom Hardy");
    }

    @Test
    @DisplayName("toEntity - should map all fields from DTO to entity")
    void toEntity_ShouldMapAllFields() {
        VideoDTO dto = VideoDTO.builder()
                .title("The Dark Knight")
                .description("Batman faces the Joker")
                .duration(152)
                .releaseYear(2008)
                .type(VideoType.FILM)
                .category(VideoCategory.ACTION)
                .rating(9.0)
                .director("Christopher Nolan")
                .build();

        Video entity = videoMapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getTitle()).isEqualTo("The Dark Knight");
        assertThat(entity.getDescription()).isEqualTo("Batman faces the Joker");
        assertThat(entity.getType()).isEqualTo(VideoType.FILM);
        assertThat(entity.getCategory()).isEqualTo(VideoCategory.ACTION);
        assertThat(entity.getRating()).isEqualTo(9.0);
    }

    @Test
    @DisplayName("updateEntityFromDTO - should update entity fields from DTO")
    void updateEntityFromDTO_ShouldUpdateFields() {
        Video entity = Video.builder()
                .id(1L)
                .title("Old Title")
                .description("Old Description")
                .type(VideoType.FILM)
                .category(VideoCategory.ACTION)
                .build();

        VideoDTO update = VideoDTO.builder()
                .title("New Title")
                .description("New Description")
                .type(VideoType.SERIE)
                .category(VideoCategory.DRAME)
                .rating(7.5)
                .build();

        videoMapper.updateEntityFromDTO(update, entity);

        assertThat(entity.getId()).isEqualTo(1L); // ID preserved
        assertThat(entity.getTitle()).isEqualTo("New Title");
        assertThat(entity.getDescription()).isEqualTo("New Description");
        assertThat(entity.getType()).isEqualTo(VideoType.SERIE);
        assertThat(entity.getCategory()).isEqualTo(VideoCategory.DRAME);
        assertThat(entity.getRating()).isEqualTo(7.5);
    }

    @Test
    @DisplayName("toDTO - should handle null entity gracefully")
    void toDTO_ShouldHandleNull() {
        VideoDTO dto = videoMapper.toDTO(null);
        assertThat(dto).isNull();
    }

    @Test
    @DisplayName("toEntity - should handle null DTO gracefully")
    void toEntity_ShouldHandleNull() {
        Video entity = videoMapper.toEntity(null);
        assertThat(entity).isNull();
    }
}
