package com.streaming.video.user_service.mapper;

import com.streaming.video.user_service.dto.*;
import com.streaming.video.user_service.entity.User;
import com.streaming.video.user_service.entity.Watchlist;
import com.streaming.video.user_service.entity.WatchHistory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for UserMapper — verifies MapStruct-generated entity ↔ DTO conversions.
 */
@SpringBootTest
@ActiveProfiles("test")
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    // ═══════════════════════════════════════════════════════════════════════════
    // User mapping
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("toUserDTO - should map user fields and exclude password")
    void toUserDTO_ShouldMapFieldsExcludePassword() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("hashedpassword")
                .build();

        UserDTO dto = userMapper.toUserDTO(user);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getUsername()).isEqualTo("testuser");
        assertThat(dto.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("toUser - should map UserCreateDTO to User entity")
    void toUser_ShouldMapCreateDTO() {
        UserCreateDTO createDTO = UserCreateDTO.builder()
                .username("newuser")
                .email("new@example.com")
                .password("password123")
                .build();

        User user = userMapper.toUser(createDTO);

        assertThat(user).isNotNull();
        assertThat(user.getId()).isNull(); // ID should be ignored
        assertThat(user.getUsername()).isEqualTo("newuser");
        assertThat(user.getEmail()).isEqualTo("new@example.com");
        assertThat(user.getPassword()).isEqualTo("password123");
    }

    @Test
    @DisplayName("updateUserFromUpdateDTO - should update only non-null fields")
    void updateUserFromUpdateDTO_ShouldUpdateNonNullFields() {
        User user = User.builder()
                .id(1L)
                .username("oldname")
                .email("old@example.com")
                .password("oldpassword")
                .build();

        UserUpdateDTO updateDTO = UserUpdateDTO.builder()
                .username("newname")
                .email(null)  // should not override
                .build();

        userMapper.updateUserFromUpdateDTO(updateDTO, user);

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getUsername()).isEqualTo("newname");
        assertThat(user.getEmail()).isEqualTo("old@example.com"); // not overwritten
        assertThat(user.getPassword()).isEqualTo("oldpassword"); // password ignored
    }

    @Test
    @DisplayName("toUserDTO - should return null for null input")
    void toUserDTO_ShouldHandleNull() {
        assertThat(userMapper.toUserDTO(null)).isNull();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Watchlist mapping
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("toWatchlistDTO - should map all watchlist fields")
    void toWatchlistDTO_ShouldMapAllFields() {
        LocalDateTime now = LocalDateTime.now();
        Watchlist watchlist = Watchlist.builder()
                .id(1L)
                .userId(10L)
                .videoId(20L)
                .addedAt(now)
                .build();

        WatchlistDTO dto = userMapper.toWatchlistDTO(watchlist);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getUserId()).isEqualTo(10L);
        assertThat(dto.getVideoId()).isEqualTo(20L);
        assertThat(dto.getAddedAt()).isEqualTo(now);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // WatchHistory mapping
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("toWatchHistoryDTO - should map all history fields")
    void toWatchHistoryDTO_ShouldMapAllFields() {
        LocalDateTime now = LocalDateTime.now();
        WatchHistory history = WatchHistory.builder()
                .id(1L)
                .userId(10L)
                .videoId(20L)
                .watchedAt(now)
                .progressTime(45)
                .completed(true)
                .build();

        WatchHistoryDTO dto = userMapper.toWatchHistoryDTO(history);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getUserId()).isEqualTo(10L);
        assertThat(dto.getVideoId()).isEqualTo(20L);
        assertThat(dto.getWatchedAt()).isEqualTo(now);
        assertThat(dto.getProgressTime()).isEqualTo(45);
        assertThat(dto.getCompleted()).isTrue();
    }

    @Test
    @DisplayName("toWatchHistoryDTO - should return null for null input")
    void toWatchHistoryDTO_ShouldHandleNull() {
        assertThat(userMapper.toWatchHistoryDTO(null)).isNull();
    }
}
