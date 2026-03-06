package com.streaming.video.user_service.repository;

import com.streaming.video.user_service.entity.User;
import com.streaming.video.user_service.entity.WatchHistory;
import com.streaming.video.user_service.entity.Watchlist;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository tests for User, Watchlist, and WatchHistory repositories.
 * Uses @DataJpaTest with H2 in-memory database.
 */
@DataJpaTest
@ActiveProfiles("test")
class RepositoryTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WatchlistRepository watchlistRepository;

    @Autowired
    private WatchHistoryRepository watchHistoryRepository;

    private User savedUser;

    @BeforeEach
    void setUp() {
        watchHistoryRepository.deleteAll();
        watchlistRepository.deleteAll();
        userRepository.deleteAll();

        savedUser = userRepository.save(User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("hashedpassword")
                .build());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // UserRepository
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("UserRepository - should find user by email")
    void shouldFindByEmail() {
        Optional<User> found = userRepository.findByEmail("test@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("UserRepository - should find user by username")
    void shouldFindByUsername() {
        Optional<User> found = userRepository.findByUsername("testuser");
        assertThat(found).isPresent();
    }

    @Test
    @DisplayName("UserRepository - should check if email exists")
    void shouldCheckEmailExists() {
        assertThat(userRepository.existsByEmail("test@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("nonexistent@example.com")).isFalse();
    }

    @Test
    @DisplayName("UserRepository - should check if username exists")
    void shouldCheckUsernameExists() {
        assertThat(userRepository.existsByUsername("testuser")).isTrue();
        assertThat(userRepository.existsByUsername("nonexistent")).isFalse();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // WatchlistRepository
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("WatchlistRepository - should save and find by userId")
    void shouldSaveAndFindWatchlist() {
        Watchlist w = watchlistRepository.save(Watchlist.builder()
                .userId(savedUser.getId())
                .videoId(10L)
                .addedAt(LocalDateTime.now())
                .build());

        List<Watchlist> result = watchlistRepository.findByUserId(savedUser.getId());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getVideoId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("WatchlistRepository - should find by userId and videoId")
    void shouldFindByUserIdAndVideoId() {
        watchlistRepository.save(Watchlist.builder()
                .userId(savedUser.getId())
                .videoId(10L)
                .addedAt(LocalDateTime.now())
                .build());

        Optional<Watchlist> result = watchlistRepository.findByUserIdAndVideoId(savedUser.getId(), 10L);
        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("WatchlistRepository - should check existence by userId and videoId")
    void shouldCheckExistsByUserIdAndVideoId() {
        watchlistRepository.save(Watchlist.builder()
                .userId(savedUser.getId())
                .videoId(10L)
                .addedAt(LocalDateTime.now())
                .build());

        assertThat(watchlistRepository.existsByUserIdAndVideoId(savedUser.getId(), 10L)).isTrue();
        assertThat(watchlistRepository.existsByUserIdAndVideoId(savedUser.getId(), 99L)).isFalse();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // WatchHistoryRepository
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("WatchHistoryRepository - should find by userId ordered by watchedAt desc")
    void shouldFindByUserIdOrdered() {
        watchHistoryRepository.save(WatchHistory.builder()
                .userId(savedUser.getId())
                .videoId(10L)
                .watchedAt(LocalDateTime.now().minusDays(2))
                .progressTime(60)
                .completed(true)
                .build());
        watchHistoryRepository.save(WatchHistory.builder()
                .userId(savedUser.getId())
                .videoId(11L)
                .watchedAt(LocalDateTime.now())
                .progressTime(30)
                .completed(false)
                .build());

        List<WatchHistory> result = watchHistoryRepository.findByUserIdOrderByWatchedAtDesc(savedUser.getId());
        assertThat(result).hasSize(2);
        // Most recent first
        assertThat(result.get(0).getVideoId()).isEqualTo(11L);
    }

    @Test
    @DisplayName("WatchHistoryRepository - should count by userId")
    void shouldCountByUserId() {
        watchHistoryRepository.save(WatchHistory.builder()
                .userId(savedUser.getId()).videoId(10L)
                .watchedAt(LocalDateTime.now()).progressTime(60).completed(true).build());
        watchHistoryRepository.save(WatchHistory.builder()
                .userId(savedUser.getId()).videoId(11L)
                .watchedAt(LocalDateTime.now()).progressTime(30).completed(false).build());

        assertThat(watchHistoryRepository.countByUserId(savedUser.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("WatchHistoryRepository - should count completed by userId")
    void shouldCountCompletedByUserId() {
        watchHistoryRepository.save(WatchHistory.builder()
                .userId(savedUser.getId()).videoId(10L)
                .watchedAt(LocalDateTime.now()).progressTime(60).completed(true).build());
        watchHistoryRepository.save(WatchHistory.builder()
                .userId(savedUser.getId()).videoId(11L)
                .watchedAt(LocalDateTime.now()).progressTime(30).completed(false).build());

        assertThat(watchHistoryRepository.countByUserIdAndCompletedTrue(savedUser.getId())).isEqualTo(1);
    }
}
