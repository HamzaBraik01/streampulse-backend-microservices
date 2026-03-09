package com.streampulse.videoservice.service;

import com.streampulse.videoservice.dto.VideoDTO;
import com.streampulse.videoservice.entity.Video;
import com.streampulse.videoservice.entity.VideoCategory;
import com.streampulse.videoservice.entity.VideoType;
import com.streampulse.videoservice.exception.VideoNotFoundException;
import com.streampulse.videoservice.mapper.VideoMapper;
import com.streampulse.videoservice.repository.VideoRepository;
import com.streampulse.videoservice.service.impl.VideoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for VideoServiceImpl — tests business logic in isolation.
 * Uses Mockito to mock repository and mapper dependencies.
 */
@ExtendWith(MockitoExtension.class)
class VideoServiceImplTest {

    @Mock
    private VideoRepository videoRepository;

    @Mock
    private VideoMapper videoMapper;

    @InjectMocks
    private VideoServiceImpl videoService;

    private Video sampleEntity;
    private VideoDTO sampleDTO;

    @BeforeEach
    void setUp() {
        sampleEntity = Video.builder()
                .id(1L)
                .title("Inception")
                .description("A skilled thief enters people's dreams")
                .thumbnailUrl("https://example.com/inception.jpg")
                .trailerUrl("https://youtube.com/embed/abc123")
                .duration(148)
                .releaseYear(2010)
                .type(VideoType.FILM)
                .category(VideoCategory.SCIENCE_FICTION)
                .rating(8.8)
                .director("Christopher Nolan")
                .cast("Leonardo DiCaprio, Tom Hardy")
                .build();

        sampleDTO = VideoDTO.builder()
                .id(1L)
                .title("Inception")
                .description("A skilled thief enters people's dreams")
                .thumbnailUrl("https://example.com/inception.jpg")
                .trailerUrl("https://youtube.com/embed/abc123")
                .duration(148)
                .releaseYear(2010)
                .type(VideoType.FILM)
                .category(VideoCategory.SCIENCE_FICTION)
                .rating(8.8)
                .director("Christopher Nolan")
                .cast("Leonardo DiCaprio, Tom Hardy")
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // findAll
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("findAll - should return list of all videos")
    void findAll_ShouldReturnAllVideos() {
        when(videoRepository.findAll()).thenReturn(List.of(sampleEntity));
        when(videoMapper.toDTO(sampleEntity)).thenReturn(sampleDTO);

        List<VideoDTO> result = videoService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Inception");
        verify(videoRepository).findAll();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // findById
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("findById - should return video when found")
    void findById_ShouldReturnVideo() {
        when(videoRepository.findById(1L)).thenReturn(Optional.of(sampleEntity));
        when(videoMapper.toDTO(sampleEntity)).thenReturn(sampleDTO);

        VideoDTO result = videoService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Inception");
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findById - should throw VideoNotFoundException when not found")
    void findById_ShouldThrowWhenNotFound() {
        when(videoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> videoService.findById(99L))
                .isInstanceOf(VideoNotFoundException.class)
                .hasMessageContaining("Video not found with id: 99");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // create
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("create - should create and return new video")
    void create_ShouldCreateVideo() {
        VideoDTO inputDTO = VideoDTO.builder()
                .title("New Movie")
                .type(VideoType.FILM)
                .category(VideoCategory.ACTION)
                .build();

        Video newEntity = Video.builder()
                .title("New Movie")
                .type(VideoType.FILM)
                .category(VideoCategory.ACTION)
                .build();

        Video savedEntity = Video.builder()
                .id(2L)
                .title("New Movie")
                .type(VideoType.FILM)
                .category(VideoCategory.ACTION)
                .build();

        VideoDTO savedDTO = VideoDTO.builder()
                .id(2L)
                .title("New Movie")
                .type(VideoType.FILM)
                .category(VideoCategory.ACTION)
                .build();

        when(videoMapper.toEntity(inputDTO)).thenReturn(newEntity);
        when(videoRepository.save(newEntity)).thenReturn(savedEntity);
        when(videoMapper.toDTO(savedEntity)).thenReturn(savedDTO);

        VideoDTO result = videoService.create(inputDTO);

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getTitle()).isEqualTo("New Movie");
        verify(videoRepository).save(any(Video.class));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // update
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("update - should update existing video")
    void update_ShouldUpdateVideo() {
        VideoDTO updateDTO = VideoDTO.builder()
                .title("Updated Title")
                .type(VideoType.FILM)
                .category(VideoCategory.SCIENCE_FICTION)
                .build();

        when(videoRepository.findById(1L)).thenReturn(Optional.of(sampleEntity));
        doNothing().when(videoMapper).updateEntityFromDTO(updateDTO, sampleEntity);
        when(videoRepository.save(sampleEntity)).thenReturn(sampleEntity);
        when(videoMapper.toDTO(sampleEntity)).thenReturn(sampleDTO);

        VideoDTO result = videoService.update(1L, updateDTO);

        assertThat(result).isNotNull();
        verify(videoMapper).updateEntityFromDTO(updateDTO, sampleEntity);
        verify(videoRepository).save(sampleEntity);
    }

    @Test
    @DisplayName("update - should throw VideoNotFoundException when not found")
    void update_ShouldThrowWhenNotFound() {
        when(videoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> videoService.update(99L, sampleDTO))
                .isInstanceOf(VideoNotFoundException.class);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // delete
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("delete - should delete existing video")
    void delete_ShouldDeleteVideo() {
        when(videoRepository.existsById(1L)).thenReturn(true);
        doNothing().when(videoRepository).deleteById(1L);

        videoService.delete(1L);

        verify(videoRepository).deleteById(1L);
    }

    @Test
    @DisplayName("delete - should throw VideoNotFoundException when not found")
    void delete_ShouldThrowWhenNotFound() {
        when(videoRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> videoService.delete(99L))
                .isInstanceOf(VideoNotFoundException.class);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // searchByTitle
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("searchByTitle - should return matching videos")
    void searchByTitle_ShouldReturnMatching() {
        when(videoRepository.findByTitleContainingIgnoreCase("incep")).thenReturn(List.of(sampleEntity));
        when(videoMapper.toDTO(sampleEntity)).thenReturn(sampleDTO);

        List<VideoDTO> result = videoService.searchByTitle("incep");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Inception");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // findByType / findByCategory
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("findByType - should return videos of specified type")
    void findByType_ShouldReturnFilteredVideos() {
        when(videoRepository.findByType(VideoType.FILM)).thenReturn(List.of(sampleEntity));
        when(videoMapper.toDTO(sampleEntity)).thenReturn(sampleDTO);

        List<VideoDTO> result = videoService.findByType(VideoType.FILM);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("findByCategory - should return videos of specified category")
    void findByCategory_ShouldReturnFilteredVideos() {
        when(videoRepository.findByCategory(VideoCategory.SCIENCE_FICTION)).thenReturn(List.of(sampleEntity));
        when(videoMapper.toDTO(sampleEntity)).thenReturn(sampleDTO);

        List<VideoDTO> result = videoService.findByCategory(VideoCategory.SCIENCE_FICTION);

        assertThat(result).hasSize(1);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // search (combined type+category)
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("search - should filter by both type and category")
    void search_ShouldFilterByTypeAndCategory() {
        when(videoRepository.findByTypeAndCategory(VideoType.FILM, VideoCategory.SCIENCE_FICTION))
                .thenReturn(List.of(sampleEntity));
        when(videoMapper.toDTO(sampleEntity)).thenReturn(sampleDTO);

        List<VideoDTO> result = videoService.search(VideoType.FILM, VideoCategory.SCIENCE_FICTION);

        assertThat(result).hasSize(1);
        verify(videoRepository).findByTypeAndCategory(VideoType.FILM, VideoCategory.SCIENCE_FICTION);
    }

    @Test
    @DisplayName("search - should return all when no filters")
    void search_ShouldReturnAllWhenNoFilters() {
        when(videoRepository.findAll()).thenReturn(List.of(sampleEntity));
        when(videoMapper.toDTO(sampleEntity)).thenReturn(sampleDTO);

        List<VideoDTO> result = videoService.search(null, null);

        assertThat(result).hasSize(1);
        verify(videoRepository).findAll();
    }
}
