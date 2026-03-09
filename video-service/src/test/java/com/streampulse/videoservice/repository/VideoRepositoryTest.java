package com.streampulse.videoservice.repository;

import com.streampulse.videoservice.entity.Video;
import com.streampulse.videoservice.entity.VideoCategory;
import com.streampulse.videoservice.entity.VideoType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository tests using @DataJpaTest with H2 in-memory database.
 * Tests JPA query methods defined in VideoRepository.
 */
@DataJpaTest
@ActiveProfiles("test")
class VideoRepositoryTest {

    @Autowired
    private VideoRepository videoRepository;

    @BeforeEach
    void setUp() {
        videoRepository.deleteAll();

        videoRepository.save(Video.builder()
                .title("Inception")
                .description("Dream within a dream")
                .duration(148)
                .releaseYear(2010)
                .type(VideoType.FILM)
                .category(VideoCategory.SCIENCE_FICTION)
                .rating(8.8)
                .director("Christopher Nolan")
                .build());

        videoRepository.save(Video.builder()
                .title("The Dark Knight")
                .description("Batman faces the Joker")
                .duration(152)
                .releaseYear(2008)
                .type(VideoType.FILM)
                .category(VideoCategory.ACTION)
                .rating(9.0)
                .director("Christopher Nolan")
                .build());

        videoRepository.save(Video.builder()
                .title("Breaking Bad")
                .description("A chemistry teacher turns to crime")
                .duration(60)
                .releaseYear(2008)
                .type(VideoType.SERIE)
                .category(VideoCategory.THRILLER)
                .rating(9.5)
                .director("Vince Gilligan")
                .build());
    }

    @Test
    @DisplayName("findByType - should return only videos of matching type")
    void findByType_ShouldReturnMatchingType() {
        List<Video> films = videoRepository.findByType(VideoType.FILM);
        List<Video> series = videoRepository.findByType(VideoType.SERIE);

        assertThat(films).hasSize(2);
        assertThat(series).hasSize(1);
        assertThat(series.get(0).getTitle()).isEqualTo("Breaking Bad");
    }

    @Test
    @DisplayName("findByCategory - should return only videos of matching category")
    void findByCategory_ShouldReturnMatchingCategory() {
        List<Video> action = videoRepository.findByCategory(VideoCategory.ACTION);
        List<Video> scifi = videoRepository.findByCategory(VideoCategory.SCIENCE_FICTION);

        assertThat(action).hasSize(1);
        assertThat(scifi).hasSize(1);
        assertThat(action.get(0).getTitle()).isEqualTo("The Dark Knight");
    }

    @Test
    @DisplayName("findByTypeAndCategory - should filter by both")
    void findByTypeAndCategory_ShouldReturnMatching() {
        List<Video> result = videoRepository.findByTypeAndCategory(VideoType.FILM, VideoCategory.ACTION);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("The Dark Knight");
    }

    @Test
    @DisplayName("findByTitleContainingIgnoreCase - should search case insensitive")
    void findByTitleContainingIgnoreCase_ShouldSearchCaseInsensitive() {
        List<Video> result = videoRepository.findByTitleContainingIgnoreCase("dark");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("The Dark Knight");
    }

    @Test
    @DisplayName("findByTitleContainingIgnoreCase - should return empty for no match")
    void findByTitleContainingIgnoreCase_ShouldReturnEmptyForNoMatch() {
        List<Video> result = videoRepository.findByTitleContainingIgnoreCase("nonexistent");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("CRUD operations - should save, find, update and delete")
    void crudOperations_ShouldWork() {
        // Create
        Video video = videoRepository.save(Video.builder()
                .title("Test Movie")
                .type(VideoType.FILM)
                .category(VideoCategory.COMEDIE)
                .build());
        assertThat(video.getId()).isNotNull();

        // Read
        var found = videoRepository.findById(video.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Test Movie");

        // Update
        found.get().setTitle("Updated Movie");
        videoRepository.save(found.get());
        var updated = videoRepository.findById(video.getId());
        assertThat(updated.get().getTitle()).isEqualTo("Updated Movie");

        // Delete
        videoRepository.deleteById(video.getId());
        assertThat(videoRepository.findById(video.getId())).isEmpty();
    }
}
