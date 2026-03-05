package com.streaming.video.user_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * OpenFeign client for communicating with video-service.
 * Used to validate that videos exist before adding to watchlist or recording history.
 */
@FeignClient(
        name = "video-service",
        url = "${feign.client.video-service.url:http://localhost:8081}",
        fallbackFactory = VideoServiceClientFallbackFactory.class
)
public interface VideoServiceClient {

    /**
     * Retrieve a video by its ID from video-service.
     */
    @GetMapping("/api/videos/{id}")
    VideoDTO getVideoById(@PathVariable("id") Long id);
}
