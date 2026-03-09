package com.streaming.video.user_service.controller;

import com.streaming.video.user_service.dto.*;
import com.streaming.video.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for User resource — handles all HTTP requests for /api/users.
 * Implements all 10 endpoints defined in the PRD (Section 10.2).
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "User management, watchlist, viewing history and statistics API")
public class UserController {

    private final UserService userService;

    // ═══════════════════════════════════════════════════════════════════════════
    // USER CRUD
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * POST /api/users — Create a new user
     */
    @PostMapping
    @Operation(summary = "Create user", description = "Register a new user account")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserCreateDTO userCreateDTO) {
        UserDTO created = userService.createUser(userCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * GET /api/users/{id} — Get user by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve a user profile by their unique identifier")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * PUT /api/users/{id} — Update user by ID (only provided fields are updated)
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Update user profile — only non-null fields are applied, password is optional")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        UserDTO updated = userService.updateUser(id, userUpdateDTO);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /api/users/{id} — Delete user by ID
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Remove a user account and all associated data")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // WATCHLIST
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * POST /api/users/{id}/watchlist/{videoId} — Add video to watchlist
     */
    @PostMapping("/{id}/watchlist/{videoId}")
    @Operation(summary = "Add to watchlist", description = "Add a video to the user's watchlist")
    public ResponseEntity<WatchlistDTO> addToWatchlist(
            @PathVariable Long id,
            @PathVariable Long videoId) {
        WatchlistDTO watchlist = userService.addToWatchlist(id, videoId);
        return ResponseEntity.status(HttpStatus.CREATED).body(watchlist);
    }

    /**
     * DELETE /api/users/{id}/watchlist/{videoId} — Remove video from watchlist
     */
    @DeleteMapping("/{id}/watchlist/{videoId}")
    @Operation(summary = "Remove from watchlist", description = "Remove a video from the user's watchlist")
    public ResponseEntity<Void> removeFromWatchlist(
            @PathVariable Long id,
            @PathVariable Long videoId) {
        userService.removeFromWatchlist(id, videoId);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/users/{id}/watchlist — Get user's watchlist
     */
    @GetMapping("/{id}/watchlist")
    @Operation(summary = "Get watchlist", description = "Retrieve all videos in the user's watchlist")
    public ResponseEntity<List<WatchlistDTO>> getWatchlist(@PathVariable Long id) {
        List<WatchlistDTO> watchlist = userService.getWatchlist(id);
        return ResponseEntity.ok(watchlist);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // VIEWING HISTORY
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * POST /api/users/{id}/history — Record a viewing event
     */
    @PostMapping("/{id}/history")
    @Operation(summary = "Record viewing event", description = "Record a video viewing session with progress and completion status")
    public ResponseEntity<WatchHistoryDTO> recordViewingEvent(
            @PathVariable Long id,
            @Valid @RequestBody WatchHistoryCreateDTO watchHistoryCreateDTO) {
        WatchHistoryDTO history = userService.recordViewingEvent(id, watchHistoryCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(history);
    }

    /**
     * GET /api/users/{id}/history — Get user's viewing history
     */
    @GetMapping("/{id}/history")
    @Operation(summary = "Get viewing history", description = "Retrieve the user's complete viewing history")
    public ResponseEntity<List<WatchHistoryDTO>> getViewingHistory(@PathVariable Long id) {
        List<WatchHistoryDTO> history = userService.getViewingHistory(id);
        return ResponseEntity.ok(history);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // VIEWING STATISTICS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/users/{id}/stats — Get user's viewing statistics
     */
    @GetMapping("/{id}/stats")
    @Operation(summary = "Get viewing statistics", description = "Retrieve viewing stats: total watched, completion rate, total watch time")
    public ResponseEntity<ViewingStatsDTO> getViewingStats(@PathVariable Long id) {
        ViewingStatsDTO stats = userService.getViewingStats(id);
        return ResponseEntity.ok(stats);
    }
}
