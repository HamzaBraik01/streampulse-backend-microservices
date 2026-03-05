package com.streaming.video.user_service.service;

import com.streaming.video.user_service.dto.*;

import java.util.List;

/**
 * Service interface for User business logic — CRUD, watchlist, history, and stats.
 */
public interface UserService {

    // ─── User CRUD ──────────────────────────────────────────────────────────────

    UserDTO createUser(UserCreateDTO userCreateDTO);

    UserDTO getUserById(Long id);

    UserDTO updateUser(Long id, UserUpdateDTO userUpdateDTO);

    void deleteUser(Long id);

    // ─── Watchlist ──────────────────────────────────────────────────────────────

    WatchlistDTO addToWatchlist(Long userId, Long videoId);

    void removeFromWatchlist(Long userId, Long videoId);

    List<WatchlistDTO> getWatchlist(Long userId);

    // ─── Viewing History ────────────────────────────────────────────────────────

    WatchHistoryDTO recordViewingEvent(Long userId, WatchHistoryCreateDTO dto);

    List<WatchHistoryDTO> getViewingHistory(Long userId);

    // ─── Viewing Statistics ─────────────────────────────────────────────────────

    ViewingStatsDTO getViewingStats(Long userId);
}
