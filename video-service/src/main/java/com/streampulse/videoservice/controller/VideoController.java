import com.streampulse.videoservice.entity.VideoType;
import com.streampulse.videoservice.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
@Tag(name = "Video Controller", description = "Endpoints for managing videos in StreamPulse")
public class VideoController {

    private final VideoService videoService;

    /**
     * GET /api/videos — returns all videos
     */
    @GetMapping
    @Operation(summary = "Get all videos", description = "Retrieve a list of all available videos")
    public ResponseEntity<List<VideoDTO>> findAll() {
        List<VideoDTO> videos = videoService.findAll();
        return ResponseEntity.ok(videos);
    }

    /**
     * GET /api/videos/{id} — returns a single video by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get video by ID", description = "Retrieve a single video's details using its unique ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Video found"),
        @ApiResponse(responseCode = "404", description = "Video not found")
    })
    public ResponseEntity<VideoDTO> findById(
            @Parameter(description = "ID of the video to retrieve", required = true)
            @PathVariable Long id) {
        VideoDTO video = videoService.findById(id);
        return ResponseEntity.ok(video);
    }

    /**
     * POST /api/videos — creates a new video
     */
    @PostMapping
    @Operation(summary = "Create a new video", description = "Register a new video in the system")
    @ApiResponse(responseCode = "201", description = "Video created successfully")
    public ResponseEntity<VideoDTO> create(@Valid @RequestBody VideoDTO videoDTO) {
        VideoDTO created = videoService.create(videoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/videos/{id} — updates an existing video
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing video", description = "Modify details of an already registered video")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Video updated successfully"),
        @ApiResponse(responseCode = "404", description = "Video not found")
    })
    public ResponseEntity<VideoDTO> update(
            @Parameter(description = "ID of the video to update", required = true)
            @PathVariable Long id,
            @Valid @RequestBody VideoDTO videoDTO) {
        VideoDTO updated = videoService.update(id, videoDTO);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /api/videos/{id} — deletes a video
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a video", description = "Permanently remove a video from the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Video deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Video not found")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID of the video to delete", required = true)
            @PathVariable Long id) {
        videoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/videos/search?type=&category= — filter videos by type and/or category
     */
    @GetMapping("/search")
    @Operation(summary = "Search and filter videos", description = "Filter videos based on their type and/or category")
    public ResponseEntity<List<VideoDTO>> search(
            @Parameter(description = "Type of video (FILM, SERIE)") @RequestParam(required = false) VideoType type,
            @Parameter(description = "Category of video") @RequestParam(required = false) VideoCategory category) {
        List<VideoDTO> videos = videoService.search(type, category);
        return ResponseEntity.ok(videos);
    }
}
