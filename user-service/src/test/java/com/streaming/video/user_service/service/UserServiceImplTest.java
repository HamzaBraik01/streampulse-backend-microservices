package com.streaming.video.user_service.service;

import com.streaming.video.user_service.client.VideoDTO;
import com.streaming.video.user_service.client.VideoServiceClient;
import com.streaming.video.user_service.dto.*;
import com.streaming.video.user_service.entity.User;
import com.streaming.video.user_service.entity.WatchHistory;
import com.streaming.video.user_service.entity.Watchlist;
import com.streaming.video.user_service.exception.UserNotFoundException;
import com.streaming.video.user_service.exception.VideoNotFoundException;
import com.streaming.video.user_service.mapper.UserMapper;
import com.streaming.video.user_service.repository.UserRepository;
import com.streaming.video.user_service.repository.WatchHistoryRepository;
import com.streaming.video.user_service.repository.WatchlistRepository;
import com.streaming.video.user_service.service.impl.UserServiceImpl;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserServiceImpl — tests all business logic in isolation.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WatchlistRepository watchlistRepository;

    @Mock
    private WatchHistoryRepository watchHistoryRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private VideoServiceClient videoServiceClient;

    @InjectMocks
    private UserServiceImpl userService;

    private User sampleUser;
    private UserDTO sampleUserDTO;
    private UserCreateDTO sampleCreateDTO;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("hashedpassword")
                .build();

        sampleUserDTO = UserDTO.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();

        sampleCreateDTO = UserCreateDTO.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // createUser
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("createUser - should create user with hashed password")
    void createUser_ShouldCreateUserWithHashedPassword() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userMapper.toUser(sampleCreateDTO)).thenReturn(sampleUser);
        when(passwordEncoder.encode("password123")).thenReturn("bcrypt_hashed");
        when(userRepository.save(sampleUser)).thenReturn(sampleUser);
        when(userMapper.toUserDTO(sampleUser)).thenReturn(sampleUserDTO);

        UserDTO result = userService.createUser(sampleCreateDTO);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(sampleUser);
    }

    @Test
    @DisplayName("createUser - should throw when email already exists")
    void createUser_ShouldThrowWhenEmailExists() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(sampleCreateDTO))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Email already in use");
    }

    @Test
    @DisplayName("createUser - should throw when username already exists")
    void createUser_ShouldThrowWhenUsernameExists() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(sampleCreateDTO))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Username already in use");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // getUserById
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getUserById - should return user when found")
    void getUserById_ShouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userMapper.toUserDTO(sampleUser)).thenReturn(sampleUserDTO);

        UserDTO result = userService.getUserById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getUserById - should throw when user not found")
    void getUserById_ShouldThrowWhenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found with id: 99");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // updateUser
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("updateUser - should update user successfully")
    void updateUser_ShouldUpdateUser() {
        UserUpdateDTO updateDTO = UserUpdateDTO.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        doNothing().when(userMapper).updateUserFromUpdateDTO(updateDTO, sampleUser);
        when(passwordEncoder.encode("password123")).thenReturn("new_hashed");
        when(userRepository.save(sampleUser)).thenReturn(sampleUser);
        when(userMapper.toUserDTO(sampleUser)).thenReturn(sampleUserDTO);

        UserDTO result = userService.updateUser(1L, updateDTO);

        assertThat(result).isNotNull();
        verify(passwordEncoder).encode("password123");
    }

    @Test
    @DisplayName("updateUser - should update without password when password is null")
    void updateUser_ShouldUpdateWithoutPasswordWhenNull() {
        UserUpdateDTO updateDTO = UserUpdateDTO.builder()
                .username("updatedname")
                .email("test@example.com")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        doNothing().when(userMapper).updateUserFromUpdateDTO(updateDTO, sampleUser);
        when(userRepository.save(sampleUser)).thenReturn(sampleUser);
        when(userMapper.toUserDTO(sampleUser)).thenReturn(sampleUserDTO);

        UserDTO result = userService.updateUser(1L, updateDTO);

        assertThat(result).isNotNull();
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("updateUser - should throw when user not found")
    void updateUser_ShouldThrowWhenNotFound() {
        UserUpdateDTO updateDTO = UserUpdateDTO.builder().username("test").build();
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(99L, updateDTO))
                .isInstanceOf(UserNotFoundException.class);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // deleteUser
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("deleteUser - should delete user successfully")
    void deleteUser_ShouldDeleteUser() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteUser - should throw when user not found")
    void deleteUser_ShouldThrowWhenNotFound() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(99L))
                .isInstanceOf(UserNotFoundException.class);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // addToWatchlist
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("addToWatchlist - should add video to user's watchlist")
    void addToWatchlist_ShouldAddVideoToWatchlist() {
        VideoDTO videoDTO = VideoDTO.builder().id(10L).title("Test").build();
        Watchlist savedWatchlist = Watchlist.builder()
                .id(1L).userId(1L).videoId(10L).addedAt(LocalDateTime.now()).build();
        WatchlistDTO watchlistDTO = WatchlistDTO.builder()
                .id(1L).userId(1L).videoId(10L).addedAt(LocalDateTime.now()).build();

        when(userRepository.existsById(1L)).thenReturn(true);
        when(videoServiceClient.getVideoById(10L)).thenReturn(videoDTO);
        when(watchlistRepository.existsByUserIdAndVideoId(1L, 10L)).thenReturn(false);
        when(watchlistRepository.save(any(Watchlist.class))).thenReturn(savedWatchlist);
        when(userMapper.toWatchlistDTO(savedWatchlist)).thenReturn(watchlistDTO);

        WatchlistDTO result = userService.addToWatchlist(1L, 10L);

        assertThat(result).isNotNull();
        assertThat(result.getVideoId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("addToWatchlist - should throw when video already in watchlist")
    void addToWatchlist_ShouldThrowWhenDuplicate() {
        VideoDTO videoDTO = VideoDTO.builder().id(10L).title("Test").build();

        when(userRepository.existsById(1L)).thenReturn(true);
        when(videoServiceClient.getVideoById(10L)).thenReturn(videoDTO);
        when(watchlistRepository.existsByUserIdAndVideoId(1L, 10L)).thenReturn(true);

        assertThatThrownBy(() -> userService.addToWatchlist(1L, 10L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already in the watchlist");
    }

    @Test
    @DisplayName("addToWatchlist - should throw when user not found")
    void addToWatchlist_ShouldThrowWhenUserNotFound() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> userService.addToWatchlist(99L, 10L))
                .isInstanceOf(UserNotFoundException.class);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // removeFromWatchlist
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("removeFromWatchlist - should remove video from watchlist")
    void removeFromWatchlist_ShouldRemoveVideo() {
        Watchlist watchlist = Watchlist.builder().id(1L).userId(1L).videoId(10L).build();

        when(userRepository.existsById(1L)).thenReturn(true);
        when(watchlistRepository.findByUserIdAndVideoId(1L, 10L)).thenReturn(Optional.of(watchlist));

        userService.removeFromWatchlist(1L, 10L);

        verify(watchlistRepository).delete(watchlist);
    }

    @Test
    @DisplayName("removeFromWatchlist - should throw when entry not found")
    void removeFromWatchlist_ShouldThrowWhenNotFound() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(watchlistRepository.findByUserIdAndVideoId(1L, 99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.removeFromWatchlist(1L, 99L))
                .isInstanceOf(VideoNotFoundException.class);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // getWatchlist
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getWatchlist - should return user's watchlist")
    void getWatchlist_ShouldReturnWatchlist() {
        Watchlist watchlist = Watchlist.builder().id(1L).userId(1L).videoId(10L).build();
        WatchlistDTO watchlistDTO = WatchlistDTO.builder().id(1L).userId(1L).videoId(10L).build();

        when(userRepository.existsById(1L)).thenReturn(true);
        when(watchlistRepository.findByUserId(1L)).thenReturn(List.of(watchlist));
        when(userMapper.toWatchlistDTO(watchlist)).thenReturn(watchlistDTO);

        List<WatchlistDTO> result = userService.getWatchlist(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getVideoId()).isEqualTo(10L);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // recordViewingEvent
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("recordViewingEvent - should record viewing event")
    void recordViewingEvent_ShouldRecordEvent() {
        VideoDTO videoDTO = VideoDTO.builder().id(10L).title("Test").build();
        WatchHistoryCreateDTO createDTO = WatchHistoryCreateDTO.builder()
                .videoId(10L).progressTime(45).completed(false).build();
        WatchHistory savedHistory = WatchHistory.builder()
                .id(1L).userId(1L).videoId(10L).progressTime(45).completed(false)
                .watchedAt(LocalDateTime.now()).build();
        WatchHistoryDTO historyDTO = WatchHistoryDTO.builder()
                .id(1L).userId(1L).videoId(10L).progressTime(45).completed(false).build();

        when(userRepository.existsById(1L)).thenReturn(true);
        when(videoServiceClient.getVideoById(10L)).thenReturn(videoDTO);
        when(watchHistoryRepository.save(any(WatchHistory.class))).thenReturn(savedHistory);
        when(userMapper.toWatchHistoryDTO(savedHistory)).thenReturn(historyDTO);

        WatchHistoryDTO result = userService.recordViewingEvent(1L, createDTO);

        assertThat(result).isNotNull();
        assertThat(result.getVideoId()).isEqualTo(10L);
        assertThat(result.getProgressTime()).isEqualTo(45);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // getViewingHistory
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getViewingHistory - should return user's viewing history")
    void getViewingHistory_ShouldReturnHistory() {
        WatchHistory history = WatchHistory.builder()
                .id(1L).userId(1L).videoId(10L).completed(true).build();
        WatchHistoryDTO historyDTO = WatchHistoryDTO.builder()
                .id(1L).userId(1L).videoId(10L).completed(true).build();

        when(userRepository.existsById(1L)).thenReturn(true);
        when(watchHistoryRepository.findByUserIdOrderByWatchedAtDesc(1L)).thenReturn(List.of(history));
        when(userMapper.toWatchHistoryDTO(history)).thenReturn(historyDTO);

        List<WatchHistoryDTO> result = userService.getViewingHistory(1L);

        assertThat(result).hasSize(1);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // getViewingStats
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getViewingStats - should calculate statistics correctly")
    void getViewingStats_ShouldCalculateStats() {
        WatchHistory h1 = WatchHistory.builder()
                .id(1L).userId(1L).videoId(10L).completed(true).progressTime(120).build();
        WatchHistory h2 = WatchHistory.builder()
                .id(2L).userId(1L).videoId(11L).completed(false).progressTime(45).build();

        when(userRepository.existsById(1L)).thenReturn(true);
        when(watchHistoryRepository.countByUserId(1L)).thenReturn(2L);
        when(watchHistoryRepository.countByUserIdAndCompletedTrue(1L)).thenReturn(1L);
        when(watchHistoryRepository.findByUserIdOrderByWatchedAtDesc(1L)).thenReturn(List.of(h1, h2));

        ViewingStatsDTO result = userService.getViewingStats(1L);

        assertThat(result.getTotalVideosWatched()).isEqualTo(2);
        assertThat(result.getCompletedVideos()).isEqualTo(1);
        assertThat(result.getCompletionRate()).isEqualTo(50.0);
        assertThat(result.getTotalWatchTimeMinutes()).isEqualTo(165);
    }

    @Test
    @DisplayName("getViewingStats - should return zero stats for user with no history")
    void getViewingStats_ShouldReturnZeroForNoHistory() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(watchHistoryRepository.countByUserId(1L)).thenReturn(0L);
        when(watchHistoryRepository.countByUserIdAndCompletedTrue(1L)).thenReturn(0L);
        when(watchHistoryRepository.findByUserIdOrderByWatchedAtDesc(1L)).thenReturn(Collections.emptyList());

        ViewingStatsDTO result = userService.getViewingStats(1L);

        assertThat(result.getTotalVideosWatched()).isZero();
        assertThat(result.getCompletionRate()).isZero();
        assertThat(result.getTotalWatchTimeMinutes()).isZero();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // validateVideoExists (Feign resilience)
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("addToWatchlist - should throw VideoNotFoundException when Feign returns 404")
    void addToWatchlist_ShouldThrowWhenVideoNotFoundViaFeign() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(videoServiceClient.getVideoById(99L)).thenThrow(FeignException.NotFound.class);

        assertThatThrownBy(() -> userService.addToWatchlist(1L, 99L))
                .isInstanceOf(VideoNotFoundException.class);
    }

    @Test
    @DisplayName("addToWatchlist - should allow operation when video-service is down (graceful degradation)")
    void addToWatchlist_ShouldAllowWhenVideoServiceDown() {
        Watchlist savedWatchlist = Watchlist.builder()
                .id(1L).userId(1L).videoId(10L).addedAt(LocalDateTime.now()).build();
        WatchlistDTO watchlistDTO = WatchlistDTO.builder()
                .id(1L).userId(1L).videoId(10L).addedAt(LocalDateTime.now()).build();

        when(userRepository.existsById(1L)).thenReturn(true);
        when(videoServiceClient.getVideoById(10L)).thenThrow(mock(FeignException.class).getClass());
        when(watchlistRepository.existsByUserIdAndVideoId(1L, 10L)).thenReturn(false);
        when(watchlistRepository.save(any(Watchlist.class))).thenReturn(savedWatchlist);
        when(userMapper.toWatchlistDTO(savedWatchlist)).thenReturn(watchlistDTO);

        WatchlistDTO result = userService.addToWatchlist(1L, 10L);

        assertThat(result).isNotNull();
    }
}
