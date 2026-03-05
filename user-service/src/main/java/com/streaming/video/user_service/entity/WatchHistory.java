package com.streaming.video.user_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * WatchHistory entity — records viewing sessions with progress tracking.
 */
@Entity
@Table(name = "watch_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "video_id", nullable = false)
    private Long videoId;

    @Column(name = "watched_at", nullable = false)
    private LocalDateTime watchedAt;

    @Column(name = "progress_time")
    private Integer progressTime;

    @Column(nullable = false)
    private Boolean completed;

    @PrePersist
    protected void onCreate() {
        if (this.watchedAt == null) {
            this.watchedAt = LocalDateTime.now();
        }
        if (this.completed == null) {
            this.completed = false;
        }
    }
}
