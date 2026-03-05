package com.streaming.video.user_service.service.impl;

import com.streaming.video.user_service.client.VideoServiceClient;
import com.streaming.video.user_service.dto.*;
import com.streaming.video.user_service.entity.User;
import com.streaming.video.user_service.entity.Watchlist;
import com.streaming.video.user_service.entity.WatchHistory;
import com.streaming.video.user_service.exception.UserNotFoundException;
import com.streaming.video.user_service.exception.VideoNotFoundException;
import com.streaming.video.user_service.mapper.UserMapper;
import com.streaming.video.user_service.repository.UserRepository;
import com.streaming.video.user_service.repository.WatchHistoryRepository;
import com.streaming.video.user_service.repository.WatchlistRepository;
import com.streaming.video.user_service.service.UserService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of UserService — contains all business logic for user management,
 * watchlist operations, viewing history, and statistics.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final WatchlistRepository watchlistRepository;
    private final WatchHistoryRepository watchHistoryRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final VideoServiceClient videoServiceClient;

    // ═══════════════════════════════════════════════════════════════════════════
    // USER CRUD
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public UserDTO createUser(UserCreateDTO userCreateDTO) {
        log.info("Creating new user: {}", userCreateDTO.getUsername());

        // Check for duplicate email/username
        if (userRepository.existsByEmail(userCreateDTO.getEmail())) {
            throw new IllegalStateException("Email already in use: " + userCreateDTO.getEmail());
        }
        if (userRepository.existsByUsername(userCreateDTO.getUsername())) {
            throw new IllegalStateException("Username already in use: " + userCreateDTO.getUsername());
        }

        User user = userMapper.toUser(userCreateDTO);
        // Hash password with BCrypt before persisting
        user.setPassword(passwordEncoder.encode(userCreateDTO.getPassword()));

        User savedUser = userRepository.save(user);
        log.info("User created with id: {}", savedUser.getId());
        return userMapper.toUserDTO(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        log.info("Fetching user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return userMapper.toUserDTO(user);
    }

    @Override
    public UserDTO updateUser(Long id, UserUpdateDTO userUpdateDTO) {
        log.info("Updating user with id: {}", id);
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        // Check uniqueness constraints for updated values
        if (userUpdateDTO.getEmail() != null
                && !existingUser.getEmail().equals(userUpdateDTO.getEmail())
                && userRepository.existsByEmail(userUpdateDTO.getEmail())) {
            throw new IllegalStateException("Email already in use: " + userUpdateDTO.getEmail());
        }
        if (userUpdateDTO.getUsername() != null
                && !existingUser.getUsername().equals(userUpdateDTO.getUsername())
                && userRepository.existsByUsername(userUpdateDTO.getUsername())) {
            throw new IllegalStateException("Username already in use: " + userUpdateDTO.getUsername());
        }

        // Apply non-null fields (username, email) via MapStruct
        userMapper.updateUserFromUpdateDTO(userUpdateDTO, existingUser);

        // Hash and update password only if provided
        if (userUpdateDTO.getPassword() != null && !userUpdateDTO.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(userUpdateDTO.getPassword()));
        }

        User updatedUser = userRepository.save(existingUser);
        log.info("User updated with id: {}", updatedUser.getId());
        return userMapper.toUserDTO(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        log.info("Deleting user with id: {}", id);
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
        log.info("User deleted with id: {}", id);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // WATCHLIST
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public WatchlistDTO addToWatchlist(Long userId, Long videoId) {
        log.info("Adding video {} to watchlist for user {}", videoId, userId);

        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }

        // Verify video exists via Feign call to video-service
        validateVideoExists(videoId);

        // Check for duplicate
        if (watchlistRepository.existsByUserIdAndVideoId(userId, videoId)) {
            throw new IllegalStateException("Video " + videoId + " is already in the watchlist");
        }

        Watchlist watchlist = Watchlist.builder()
                .userId(userId)
                .videoId(videoId)
                .build();

        Watchlist saved = watchlistRepository.save(watchlist);
        log.info("Video {} added to watchlist for user {}", videoId, userId);
        return userMapper.toWatchlistDTO(saved);
    }

    @Override
    public void removeFromWatchlist(Long userId, Long videoId) {
        log.info("Removing video {} from watchlist for user {}", videoId, userId);

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }

        Watchlist watchlist = watchlistRepository.findByUserIdAndVideoId(userId, videoId)
                .orElseThrow(() -> new VideoNotFoundException(
                        "Video " + videoId + " not found in watchlist for user " + userId));

        watchlistRepository.delete(watchlist);
        log.info("Video {} removed from watchlist for user {}", videoId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WatchlistDTO> getWatchlist(Long userId) {
        log.info("Fetching watchlist for user {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }

        return watchlistRepository.findByUserId(userId)
                .stream()
                .map(userMapper::toWatchlistDTO)
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // VIEWING HISTORY
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public WatchHistoryDTO recordViewingEvent(Long userId, WatchHistoryCreateDTO dto) {
        log.info("Recording viewing event for user {} on video {}", userId, dto.getVideoId());

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }

        // Verify video exists
        validateVideoExists(dto.getVideoId());

        WatchHistory watchHistory = WatchHistory.builder()
                .userId(userId)
                .videoId(dto.getVideoId())
                .watchedAt(LocalDateTime.now())
                .progressTime(dto.getProgressTime())
                .completed(dto.getCompleted() != null ? dto.getCompleted() : false)
                .build();

        WatchHistory saved = watchHistoryRepository.save(watchHistory);
        log.info("Viewing event recorded with id: {}", saved.getId());
        return userMapper.toWatchHistoryDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WatchHistoryDTO> getViewingHistory(Long userId) {
        log.info("Fetching viewing history for user {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }

        return watchHistoryRepository.findByUserIdOrderByWatchedAtDesc(userId)
                .stream()
                .map(userMapper::toWatchHistoryDTO)
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // VIEWING STATISTICS
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public ViewingStatsDTO getViewingStats(Long userId) {
        log.info("Fetching viewing statistics for user {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }

        long totalWatched = watchHistoryRepository.countByUserId(userId);
        long completedCount = watchHistoryRepository.countByUserIdAndCompletedTrue(userId);
        double completionRate = totalWatched > 0
                ? (double) completedCount / totalWatched * 100.0
                : 0.0;

        // Calculate total watch time from progress times
        List<WatchHistory> history = watchHistoryRepository.findByUserIdOrderByWatchedAtDesc(userId);
        long totalWatchTimeMinutes = history.stream()
                .filter(h -> h.getProgressTime() != null)
                .mapToLong(WatchHistory::getProgressTime)
                .sum();

        return ViewingStatsDTO.builder()
                .userId(userId)
                .totalVideosWatched(totalWatched)
                .completedVideos(completedCount)
                .completionRate(Math.round(completionRate * 100.0) / 100.0)
                .totalWatchTimeMinutes(totalWatchTimeMinutes)
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Validates that a video exists by calling video-service via OpenFeign.
     * Falls back gracefully if video-service is unavailable.
     */
    private void validateVideoExists(Long videoId) {
        try {
            videoServiceClient.getVideoById(videoId);
        } catch (FeignException.NotFound e) {
            throw new VideoNotFoundException("Video not found with id: " + videoId);
        } catch (FeignException e) {
            log.warn("Video service unavailable, skipping video validation for id: {}", videoId);
            // Allow the operation if video-service is down (graceful degradation)
        }
    }
}
