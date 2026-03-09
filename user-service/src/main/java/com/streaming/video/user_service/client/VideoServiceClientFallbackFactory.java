package com.streaming.video.user_service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * Fallback factory for VideoServiceClient — provides graceful degradation
 * when video-service is unavailable. Returns null to signal the caller
 * that the video service is down, allowing the operation to proceed.
 */
@Component
@Slf4j
public class VideoServiceClientFallbackFactory implements FallbackFactory<VideoServiceClient> {

    @Override
    public VideoServiceClient create(Throwable cause) {
        return new VideoServiceClient() {
            @Override
            public VideoDTO getVideoById(Long id) {
                log.warn("Video service is unavailable. Fallback triggered for video id: {}. Cause: {}",
                        id, cause.getMessage());
                return null; // Return null to indicate service unavailable
            }
        };
    }
}
