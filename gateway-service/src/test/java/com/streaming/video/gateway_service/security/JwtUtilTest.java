package com.streaming.video.gateway_service.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for JwtUtil — validates token operations in the gateway.
 */
class JwtUtilTest {

    // Use the same secret as in application.yml
    private static final String SECRET = "bXlTdXBlclNlY3JldEtleUZvclN0cmVhbVB1bHNlSldUQXV0aGVudGljYXRpb24yMDI2";

    private final JwtUtil jwtUtil = new JwtUtil(SECRET);

    private String generateTestToken(String username, String roles, long expirationMillis) {
        io.jsonwebtoken.io.Decoders decoders = null;
        javax.crypto.SecretKey key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                io.jsonwebtoken.io.Decoders.BASE64.decode(SECRET));

        java.util.Date now = new java.util.Date();
        java.util.Date expiry = new java.util.Date(now.getTime() + expirationMillis);

        return io.jsonwebtoken.Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    @Test
    @DisplayName("validateToken - should return true for valid token")
    void validateToken_ShouldReturnTrueForValidToken() {
        String token = generateTestToken("testuser", "ROLE_USER", 86400000);

        boolean result = jwtUtil.validateToken(token);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("validateToken - should return false for expired token")
    void validateToken_ShouldReturnFalseForExpiredToken() {
        String token = generateTestToken("testuser", "ROLE_USER", -1000); // already expired

        boolean result = jwtUtil.validateToken(token);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("validateToken - should return false for malformed token")
    void validateToken_ShouldReturnFalseForMalformedToken() {
        boolean result = jwtUtil.validateToken("this.is.not.a.valid.token");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("validateToken - should return false for empty token")
    void validateToken_ShouldReturnFalseForEmptyToken() {
        boolean result = jwtUtil.validateToken("");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("getUsernameFromToken - should extract username")
    void getUsernameFromToken_ShouldExtractUsername() {
        String token = generateTestToken("admin", "ROLE_ADMIN", 86400000);

        String username = jwtUtil.getUsernameFromToken(token);

        assertThat(username).isEqualTo("admin");
    }

    @Test
    @DisplayName("getRolesFromToken - should extract roles")
    void getRolesFromToken_ShouldExtractRoles() {
        String token = generateTestToken("testuser", "ROLE_USER", 86400000);

        String roles = jwtUtil.getRolesFromToken(token);

        assertThat(roles).isEqualTo("ROLE_USER");
    }
}
