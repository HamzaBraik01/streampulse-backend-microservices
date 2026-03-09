package com.streaming.video.gateway_service.security;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

/**
 * Route Validator — determines which routes are open (no auth required)
 * and which are secured (require a valid JWT token).
 */
@Component
public class RouteValidator {

    /**
     * List of open API endpoints that do not require authentication.
     */
    public static final List<String> OPEN_ENDPOINTS = List.of(
            "/api/auth/register",
            "/api/auth/login",
            "/actuator",
            "/swagger-ui",
            "/v3/api-docs",
            "/eureka"
    );

    /**
     * Predicate to check if a request targets a secured endpoint.
     */
    public Predicate<ServerHttpRequest> isSecured =
            request -> OPEN_ENDPOINTS.stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));
}

