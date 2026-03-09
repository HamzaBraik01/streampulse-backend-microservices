package com.streaming.video.gateway_service.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for RouteValidator — verifies open vs secured route determination.
 */
class RouteValidatorTest {

    private final RouteValidator routeValidator = new RouteValidator();

    @Test
    @DisplayName("isSecured - should return false for /api/auth/register")
    void isSecured_ShouldReturnFalseForRegister() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8080/api/auth/register")
                .build();

        boolean secured = routeValidator.isSecured.test(request);

        assertThat(secured).isFalse();
    }

    @Test
    @DisplayName("isSecured - should return false for /api/auth/login")
    void isSecured_ShouldReturnFalseForLogin() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8080/api/auth/login")
                .build();

        boolean secured = routeValidator.isSecured.test(request);

        assertThat(secured).isFalse();
    }

    @Test
    @DisplayName("isSecured - should return false for /actuator/health")
    void isSecured_ShouldReturnFalseForActuator() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8080/actuator/health")
                .build();

        boolean secured = routeValidator.isSecured.test(request);

        assertThat(secured).isFalse();
    }

    @Test
    @DisplayName("isSecured - should return false for /swagger-ui/index.html")
    void isSecured_ShouldReturnFalseForSwagger() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8080/swagger-ui/index.html")
                .build();

        boolean secured = routeValidator.isSecured.test(request);

        assertThat(secured).isFalse();
    }

    @Test
    @DisplayName("isSecured - should return false for /v3/api-docs")
    void isSecured_ShouldReturnFalseForApiDocs() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8080/v3/api-docs")
                .build();

        boolean secured = routeValidator.isSecured.test(request);

        assertThat(secured).isFalse();
    }

    @Test
    @DisplayName("isSecured - should return true for /api/videos")
    void isSecured_ShouldReturnTrueForVideos() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8080/api/videos")
                .build();

        boolean secured = routeValidator.isSecured.test(request);

        assertThat(secured).isTrue();
    }

    @Test
    @DisplayName("isSecured - should return true for /api/users/1")
    void isSecured_ShouldReturnTrueForUsers() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8080/api/users/1")
                .build();

        boolean secured = routeValidator.isSecured.test(request);

        assertThat(secured).isTrue();
    }

    @Test
    @DisplayName("isSecured - should return true for /api/users/1/watchlist")
    void isSecured_ShouldReturnTrueForWatchlist() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8080/api/users/1/watchlist")
                .build();

        boolean secured = routeValidator.isSecured.test(request);

        assertThat(secured).isTrue();
    }
}
