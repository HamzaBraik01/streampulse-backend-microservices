package com.streampulse.videoservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration for TMDb (The Movie Database) API integration.
 * Provides a pre-configured WebClient for making external API calls.
 * @RefreshScope allows config changes to be applied at runtime via /actuator/refresh.
 */
@Configuration
@RefreshScope
public class TmdbConfig {

    @Value("${tmdb.api.base-url:https://api.themoviedb.org/3}")
    private String baseUrl;

    @Value("${tmdb.api.key:}")
    private String apiKey;

    @Bean
    public WebClient tmdbWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Accept", "application/json")
                .build();
    }

    public String getApiKey() {
        return apiKey;
    }
}
