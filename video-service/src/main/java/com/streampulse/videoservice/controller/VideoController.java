package com.streampulse.videoservice.controller;

import com.streampulse.videoservice.dto.VideoDTO;
import com.streampulse.videoservice.entity.VideoCategory;
import com.streampulse.videoservice.entity.VideoType;
import com.streampulse.videoservice.service.VideoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    /**
     * GET /api/videos — returns all videos
     */
    @GetMapping
    public ResponseEntity<List<VideoDTO>> findAll() {
        List<VideoDTO> videos = videoService.findAll();
        return ResponseEntity.ok(videos);
    }

    /**
     * GET /api/videos/{id} — returns a single video by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<VideoDTO> findById(@PathVariable Long id) {
        VideoDTO video = videoService.findById(id);
        return ResponseEntity.ok(video);
    }

    /**
     * POST /api/videos — creates a new video
     */
    @PostMapping
    public ResponseEntity<VideoDTO> create(@Valid @RequestBody VideoDTO videoDTO) {
        VideoDTO created = videoService.create(videoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/videos/{id} — updates an existing video
     */
    @PutMapping("/{id}")
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
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        videoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/videos/search?type=&category= — filter videos by type and/or category
     */
    @GetMapping("/search")
    public ResponseEntity<List<VideoDTO>> search(
            @RequestParam(required = false) VideoType type,
            @RequestParam(required = false) VideoCategory category) {
        List<VideoDTO> videos = videoService.search(type, category);
        return ResponseEntity.ok(videos);
    }
}
