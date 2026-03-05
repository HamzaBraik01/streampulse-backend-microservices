package com.streaming.video.user_service.repository;

import com.streaming.video.user_service.entity.WatchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for WatchHistory entity.
 */
@Repository
public interface WatchHistoryRepository extends JpaRepository<WatchHistory, Long> {

    List<WatchHistory> findByUserIdOrderByWatchedAtDesc(Long userId);

    long countByUserId(Long userId);

    long countByUserIdAndCompletedTrue(Long userId);
}
