package com.streampulse.videoservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streampulse.videoservice.dto.VideoDTO;
import com.streampulse.videoservice.entity.VideoCategory;
import com.streampulse.videoservice.entity.VideoType;
import com.streampulse.videoservice.exception.VideoNotFoundException;
import com.streampulse.videoservice.service.TmdbService;
import com.streampulse.videoservice.service.VideoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for VideoController — tests REST endpoints using MockMvc.
 * Uses @WebMvcTest to test the controller layer in isolation.
 */
@WebMvcTest(VideoController.class)
class VideoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VideoService videoService;

    @MockBean
    private TmdbService tmdbService;

    private VideoDTO sampleVideo;
    private VideoDTO sampleVideo2;

    @BeforeEach
    void setUp() {
        sampleVideo = VideoDTO.builder()
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

        sampleVideo2 = VideoDTO.builder()
                .id(2L)
                .title("The Dark Knight")
                .description("Batman faces the Joker")
                .duration(152)
                .releaseYear(2008)
                .type(VideoType.FILM)
                .category(VideoCategory.ACTION)
                .rating(9.0)
                .director("Christopher Nolan")
                .cast("Christian Bale, Heath Ledger")
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // GET /api/videos
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("GET /api/videos - should return all videos with 200 OK")
    void findAll_ShouldReturnAllVideos() throws Exception {
        List<VideoDTO> videos = Arrays.asList(sampleVideo, sampleVideo2);
        when(videoService.findAll()).thenReturn(videos);

        mockMvc.perform(get("/api/videos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title").value("Inception"))
                .andExpect(jsonPath("$[1].title").value("The Dark Knight"));

        verify(videoService).findAll();
    }

    @Test
    @DisplayName("GET /api/videos - should return empty list when no videos exist")
    void findAll_ShouldReturnEmptyList() throws Exception {
        when(videoService.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/videos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // GET /api/videos/{id}
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("GET /api/videos/{id} - should return video with 200 OK")
    void findById_ShouldReturnVideo() throws Exception {
        when(videoService.findById(1L)).thenReturn(sampleVideo);

        mockMvc.perform(get("/api/videos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Inception"))
                .andExpect(jsonPath("$.type").value("SCIENCE_FICTION").doesNotExist())
                .andExpect(jsonPath("$.director").value("Christopher Nolan"));

        verify(videoService).findById(1L);
    }

    @Test
    @DisplayName("GET /api/videos/{id} - should return 404 when video not found")
    void findById_ShouldReturn404WhenNotFound() throws Exception {
        when(videoService.findById(99L)).thenThrow(new VideoNotFoundException("Video not found with id: 99"));

        mockMvc.perform(get("/api/videos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // POST /api/videos
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("POST /api/videos - should create video with 201 Created")
    void create_ShouldCreateVideo() throws Exception {
        VideoDTO inputDTO = VideoDTO.builder()
                .title("New Movie")
                .description("A great movie")
                .duration(120)
                .releaseYear(2024)
                .type(VideoType.FILM)
                .category(VideoCategory.ACTION)
                .rating(7.5)
                .build();

        VideoDTO savedDTO = VideoDTO.builder()
                .id(3L)
                .title("New Movie")
                .description("A great movie")
                .duration(120)
                .releaseYear(2024)
                .type(VideoType.FILM)
                .category(VideoCategory.ACTION)
                .rating(7.5)
                .build();

        when(videoService.create(any(VideoDTO.class))).thenReturn(savedDTO);

        mockMvc.perform(post("/api/videos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.title").value("New Movie"));

        verify(videoService).create(any(VideoDTO.class));
    }

    @Test
    @DisplayName("POST /api/videos - should return 400 when title is blank")
    void create_ShouldReturn400WhenTitleBlank() throws Exception {
        VideoDTO invalid = VideoDTO.builder()
                .title("")
                .type(VideoType.FILM)
                .category(VideoCategory.ACTION)
                .build();

        mockMvc.perform(post("/api/videos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.title").exists());
    }

    @Test
    @DisplayName("POST /api/videos - should return 400 when type is null")
    void create_ShouldReturn400WhenTypeNull() throws Exception {
        VideoDTO invalid = VideoDTO.builder()
                .title("Test Movie")
                .category(VideoCategory.ACTION)
                .build();

        mockMvc.perform(post("/api/videos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.type").exists());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PUT /api/videos/{id}
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("PUT /api/videos/{id} - should update video with 200 OK")
    void update_ShouldUpdateVideo() throws Exception {
        VideoDTO updateDTO = VideoDTO.builder()
                .title("Updated Title")
                .description("Updated description")
                .duration(150)
                .releaseYear(2010)
                .type(VideoType.FILM)
                .category(VideoCategory.SCIENCE_FICTION)
                .rating(9.0)
                .build();

        VideoDTO updatedVideo = VideoDTO.builder()
                .id(1L)
                .title("Updated Title")
                .description("Updated description")
                .duration(150)
                .releaseYear(2010)
                .type(VideoType.FILM)
                .category(VideoCategory.SCIENCE_FICTION)
                .rating(9.0)
                .build();

        when(videoService.update(eq(1L), any(VideoDTO.class))).thenReturn(updatedVideo);

        mockMvc.perform(put("/api/videos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.rating").value(9.0));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DELETE /api/videos/{id}
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("DELETE /api/videos/{id} - should delete video with 204 No Content")
    void delete_ShouldDeleteVideo() throws Exception {
        doNothing().when(videoService).delete(1L);

        mockMvc.perform(delete("/api/videos/1"))
                .andExpect(status().isNoContent());

        verify(videoService).delete(1L);
    }

    @Test
    @DisplayName("DELETE /api/videos/{id} - should return 404 when video not found")
    void delete_ShouldReturn404WhenNotFound() throws Exception {
        doThrow(new VideoNotFoundException("Video not found with id: 99"))
                .when(videoService).delete(99L);

        mockMvc.perform(delete("/api/videos/99"))
                .andExpect(status().isNotFound());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // GET /api/videos/search?title=
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("GET /api/videos/search?title= - should search videos by title")
    void searchByTitle_ShouldReturnMatchingVideos() throws Exception {
        when(videoService.searchByTitle("Inception")).thenReturn(List.of(sampleVideo));

        mockMvc.perform(get("/api/videos/search").param("title", "Inception"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Inception"));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // GET /api/videos/type/{type}
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("GET /api/videos/type/{type} - should filter by type")
    void findByType_ShouldReturnFilteredVideos() throws Exception {
        when(videoService.findByType(VideoType.FILM)).thenReturn(Arrays.asList(sampleVideo, sampleVideo2));

        mockMvc.perform(get("/api/videos/type/FILM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // GET /api/videos/category/{category}
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("GET /api/videos/category/{category} - should filter by category")
    void findByCategory_ShouldReturnFilteredVideos() throws Exception {
        when(videoService.findByCategory(VideoCategory.ACTION)).thenReturn(List.of(sampleVideo2));

        mockMvc.perform(get("/api/videos/category/ACTION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("The Dark Knight"));
    }
}
