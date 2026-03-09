package com.streaming.video.user_service.exception;

/**
 * Thrown when a referenced video is not found in video-service.
 */
public class VideoNotFoundException extends RuntimeException {

    public VideoNotFoundException(String message) {
        super(message);
    }

    public VideoNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
