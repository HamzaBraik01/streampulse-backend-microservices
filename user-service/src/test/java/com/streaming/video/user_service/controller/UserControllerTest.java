package com.streaming.video.user_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streaming.video.user_service.config.SecurityConfig;
import com.streaming.video.user_service.dto.*;
import com.streaming.video.user_service.exception.UserNotFoundException;
import com.streaming.video.user_service.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for UserController — tests REST endpoints using MockMvc.
 * Includes SecurityConfig to provide PasswordEncoder and SecurityFilterChain beans.
 */
@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserDTO sampleUserDTO;
    private UserCreateDTO sampleCreateDTO;

    @BeforeEach
    void setUp() {
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
    // USER CRUD
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("POST /api/users - should create user with 201")
    void createUser_ShouldReturnCreated() throws Exception {
        when(userService.createUser(any(UserCreateDTO.class))).thenReturn(sampleUserDTO);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @DisplayName("POST /api/users - should return 400 when validation fails")
    void createUser_ShouldReturn400WhenInvalid() throws Exception {
        UserCreateDTO invalid = UserCreateDTO.builder()
                .username("ab")  // too short
                .email("invalid")  // not email
                .password("12345")  // too short
                .build();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").exists());
    }

    @Test
    @DisplayName("GET /api/users/{id} - should return user with 200")
    void getUserById_ShouldReturnUser() throws Exception {
        when(userService.getUserById(1L)).thenReturn(sampleUserDTO);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @DisplayName("GET /api/users/{id} - should return 404 when not found")
    void getUserById_ShouldReturn404() throws Exception {
        when(userService.getUserById(99L)).thenThrow(new UserNotFoundException("User not found with id: 99"));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PUT /api/users/{id} - should update user with 200")
    void updateUser_ShouldReturnUpdated() throws Exception {
        UserUpdateDTO updateDTO = UserUpdateDTO.builder()
                .username("testuser")
                .email("test@example.com")
                .build();
        when(userService.updateUser(eq(1L), any(UserUpdateDTO.class))).thenReturn(sampleUserDTO);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - should delete with 204")
    void deleteUser_ShouldReturnNoContent() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // WATCHLIST
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("POST /api/users/{id}/watchlist/{videoId} - should add to watchlist with 201")
    void addToWatchlist_ShouldReturnCreated() throws Exception {
        WatchlistDTO watchlistDTO = WatchlistDTO.builder()
                .id(1L).userId(1L).videoId(10L).addedAt(LocalDateTime.now()).build();
        when(userService.addToWatchlist(1L, 10L)).thenReturn(watchlistDTO);

        mockMvc.perform(post("/api/users/1/watchlist/10"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.videoId").value(10));
    }

    @Test
    @DisplayName("DELETE /api/users/{id}/watchlist/{videoId} - should remove with 204")
    void removeFromWatchlist_ShouldReturnNoContent() throws Exception {
        doNothing().when(userService).removeFromWatchlist(1L, 10L);

        mockMvc.perform(delete("/api/users/1/watchlist/10"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/users/{id}/watchlist - should return watchlist with 200")
    void getWatchlist_ShouldReturnList() throws Exception {
        WatchlistDTO w = WatchlistDTO.builder()
                .id(1L).userId(1L).videoId(10L).addedAt(LocalDateTime.now()).build();
        when(userService.getWatchlist(1L)).thenReturn(List.of(w));

        mockMvc.perform(get("/api/users/1/watchlist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // VIEWING HISTORY
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("POST /api/users/{id}/history - should record event with 201")
    void recordViewingEvent_ShouldReturnCreated() throws Exception {
        WatchHistoryCreateDTO createDTO = WatchHistoryCreateDTO.builder()
                .videoId(10L).progressTime(45).completed(false).build();
        WatchHistoryDTO historyDTO = WatchHistoryDTO.builder()
                .id(1L).userId(1L).videoId(10L).progressTime(45).completed(false)
                .watchedAt(LocalDateTime.now()).build();

        when(userService.recordViewingEvent(eq(1L), any(WatchHistoryCreateDTO.class))).thenReturn(historyDTO);

        mockMvc.perform(post("/api/users/1/history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.videoId").value(10))
                .andExpect(jsonPath("$.progressTime").value(45));
    }

    @Test
    @DisplayName("GET /api/users/{id}/history - should return history with 200")
    void getViewingHistory_ShouldReturnList() throws Exception {
        WatchHistoryDTO h = WatchHistoryDTO.builder()
                .id(1L).userId(1L).videoId(10L).completed(true).build();
        when(userService.getViewingHistory(1L)).thenReturn(List.of(h));

        mockMvc.perform(get("/api/users/1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // VIEWING STATISTICS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("GET /api/users/{id}/stats - should return stats with 200")
    void getViewingStats_ShouldReturnStats() throws Exception {
        ViewingStatsDTO stats = ViewingStatsDTO.builder()
                .userId(1L).totalVideosWatched(5).completedVideos(3)
                .completionRate(60.0).totalWatchTimeMinutes(300).build();
        when(userService.getViewingStats(1L)).thenReturn(stats);

        mockMvc.perform(get("/api/users/1/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVideosWatched").value(5))
                .andExpect(jsonPath("$.completedVideos").value(3))
                .andExpect(jsonPath("$.completionRate").value(60.0));
    }
}
