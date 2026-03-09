package com.streampulse.videoservice.controller;

import com.streampulse.videoservice.dto.TmdbMovieDTO;
import com.streampulse.videoservice.dto.VideoDTO;
import com.streampulse.videoservice.entity.VideoCategory;
import com.streampulse.videoservice.entity.VideoType;
import com.streampulse.videoservice.service.TmdbService;
import com.streampulse.videoservice.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Video resource — handles all HTTP requests for /api/videos.
 * Includes CRUD operations, search/filtering, and TMDb metadata enrichment.
 */
@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
@Tag(name = "Video", description = "Video content management API")
public class VideoController {

    private final VideoService videoService;
    private final TmdbService tmdbService;

    /**
     * GET /api/videos — returns all videos, with optional type and category filters.
     *
     * @param type     optional filter by VideoType (FILM/SERIE)
     * @param category optional filter by VideoCategory
     * @return filtered or full list of videos
     */
    @GetMapping
    @Operation(summary = "Get all videos", description = "Retrieve all videos with optional type and category filters")
    public ResponseEntity<List<VideoDTO>> findAll(
            @RequestParam(required = false) VideoType type,
            @RequestParam(required = false) VideoCategory category) {
        List<VideoDTO> videos;
        if (type != null || category != null) {
            videos = videoService.search(type, category);
        } else {
            videos = videoService.findAll();
        }
        return ResponseEntity.ok(videos);
    }

    /**
     * GET /api/videos/{id} — returns a single video by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get video by ID", description = "Retrieve a single video by its unique identifier")
    public ResponseEntity<VideoDTO> findById(@PathVariable Long id) {
        VideoDTO video = videoService.findById(id);
        return ResponseEntity.ok(video);
    }

    /**
     * POST /api/videos — creates a new video
     */
    @PostMapping
    @Operation(summary = "Create a video", description = "Add a new video entry to the catalog")
    public ResponseEntity<VideoDTO> create(@Valid @RequestBody VideoDTO videoDTO) {
        VideoDTO created = videoService.create(videoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/videos/{id} — updates an existing video
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update a video", description = "Update an existing video by ID")
    public ResponseEntity<VideoDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody VideoDTO videoDTO) {
        VideoDTO updated = videoService.update(id, videoDTO);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /api/videos/{id} — deletes a video
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a video", description = "Remove a video entry from the catalog")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        videoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/videos/search?title= — search videos by title
     */
    @GetMapping("/search")
    @Operation(summary = "Search by title", description = "Search videos by title (case-insensitive partial match)")
    public ResponseEntity<List<VideoDTO>> searchByTitle(
            @RequestParam(required = false) String title) {
        List<VideoDTO> videos = videoService.searchByTitle(title);
        return ResponseEntity.ok(videos);
    }

    /**
     * GET /api/videos/type/{type} — filter videos by type (FILM/SERIE)
     */
    @GetMapping("/type/{type}")
    @Operation(summary = "Filter by type", description = "Retrieve videos filtered by type (FILM or SERIE)")
    public ResponseEntity<List<VideoDTO>> findByType(@PathVariable VideoType type) {
        List<VideoDTO> videos = videoService.findByType(type);
        return ResponseEntity.ok(videos);
    }

    /**
     * GET /api/videos/category/{category} — filter videos by category
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "Filter by category", description = "Retrieve videos filtered by category")
    public ResponseEntity<List<VideoDTO>> findByCategory(@PathVariable VideoCategory category) {
        List<VideoDTO> videos = videoService.findByCategory(category);
        return ResponseEntity.ok(videos);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TMDb API INTEGRATION (VS-06)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/videos/tmdb/search?title= — Search TMDb for movies by title
     */
    @GetMapping("/tmdb/search")
    @Operation(summary = "Search TMDb", description = "Search The Movie Database for movies by title")
    public ResponseEntity<List<TmdbMovieDTO>> searchTmdb(@RequestParam String title) {
        List<TmdbMovieDTO> results = tmdbService.searchMovies(title);
        return ResponseEntity.ok(results);
    }

    /**
     * POST /api/videos/tmdb/import/{tmdbId} — Import a movie from TMDb by its TMDb ID
     */
    @PostMapping("/tmdb/import/{tmdbId}")
    @Operation(summary = "Import from TMDb", description = "Import a movie from TMDb by ID and save to local catalog")
    public ResponseEntity<VideoDTO> importFromTmdb(@PathVariable Long tmdbId) {
        VideoDTO imported = tmdbService.importMovie(tmdbId);
        return ResponseEntity.status(HttpStatus.CREATED).body(imported);
    }
}
