package com.streampulse.videoservice.service.impl;

import com.streampulse.videoservice.config.TmdbConfig;
import com.streampulse.videoservice.dto.*;
import com.streampulse.videoservice.entity.Video;
import com.streampulse.videoservice.entity.VideoCategory;
import com.streampulse.videoservice.entity.VideoType;
import com.streampulse.videoservice.mapper.VideoMapper;
import com.streampulse.videoservice.repository.VideoRepository;
import com.streampulse.videoservice.service.TmdbService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Implementation of TmdbService — integrates with The Movie Database (TMDb) API
 * to search for movies and import metadata into the local video catalog.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TmdbServiceImpl implements TmdbService {

    private final WebClient tmdbWebClient;
    private final TmdbConfig tmdbConfig;
    private final VideoRepository videoRepository;
    private final VideoMapper videoMapper;

    /**
     * TMDb genre ID to VideoCategory mapping.
     * See: https://developer.themoviedb.org/reference/genre-movie-list
     */
    private static final Map<Integer, VideoCategory> GENRE_MAPPING = Map.of(
            28, VideoCategory.ACTION,
            35, VideoCategory.COMEDIE,
            18, VideoCategory.DRAME,
            878, VideoCategory.SCIENCE_FICTION,
            53, VideoCategory.THRILLER,
            27, VideoCategory.HORREUR
    );

    @Override
    public List<TmdbMovieDTO> searchMovies(String title) {
        log.info("Searching TMDb for movies with title: {}", title);
        String apiKey = tmdbConfig.getApiKey();
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("TMDb API key is not configured. Set tmdb.api.key in application config.");
            return Collections.emptyList();
        }

        try {
            TmdbSearchResponse response = tmdbWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search/movie")
                            .queryParam("api_key", apiKey)
                            .queryParam("query", title)
                            .queryParam("language", "en-US")
                            .queryParam("page", 1)
                            .build())
                    .retrieve()
                    .bodyToMono(TmdbSearchResponse.class)
                    .block();

            if (response != null && response.getResults() != null) {
                log.info("TMDb returned {} results for '{}'", response.getResults().size(), title);
                return response.getResults();
            }
            return Collections.emptyList();
        } catch (WebClientResponseException e) {
            log.error("TMDb API error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error calling TMDb API: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public VideoDTO importMovie(Long tmdbId) {
        log.info("Importing movie from TMDb with id: {}", tmdbId);
        String apiKey = tmdbConfig.getApiKey();
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("TMDb API key is not configured. Set tmdb.api.key in application config.");
        }

        try {
            TmdbMovieDetailDTO detail = tmdbWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/movie/{id}")
                            .queryParam("api_key", apiKey)
                            .queryParam("append_to_response", "credits,videos")
                            .queryParam("language", "en-US")
                            .build(tmdbId))
                    .retrieve()
                    .bodyToMono(TmdbMovieDetailDTO.class)
                    .block();

            if (detail == null) {
                throw new IllegalStateException("No data returned from TMDb for movie id: " + tmdbId);
            }

            // Map TMDb data to our Video entity
            Video video = Video.builder()
                    .title(detail.getTitle())
                    .description(detail.getOverview())
                    .thumbnailUrl(detail.getFullPosterUrl())
                    .trailerUrl(detail.getTrailerUrl())
                    .duration(detail.getRuntime())
                    .releaseYear(detail.getReleaseYear())
                    .type(VideoType.FILM) // TMDb primarily returns films
                    .category(mapGenreToCategory(detail))
                    .rating(detail.getVoteAverage())
                    .director(detail.getDirector())
                    .cast(detail.getCastString())
                    .build();

            Video savedVideo = videoRepository.save(video);
            log.info("Successfully imported movie '{}' from TMDb (id: {})", detail.getTitle(), tmdbId);
            return videoMapper.toDTO(savedVideo);

        } catch (WebClientResponseException.NotFound e) {
            throw new IllegalStateException("Movie not found on TMDb with id: " + tmdbId);
        } catch (WebClientResponseException e) {
            log.error("TMDb API error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new IllegalStateException("Failed to fetch movie from TMDb: " + e.getMessage());
        }
    }

    /**
     * Maps TMDb genre to our VideoCategory enum.
     * Uses the first matching genre, defaults to ACTION.
     */
    private VideoCategory mapGenreToCategory(TmdbMovieDetailDTO detail) {
        if (detail.getGenres() != null) {
            for (TmdbMovieDetailDTO.Genre genre : detail.getGenres()) {
                VideoCategory mapped = GENRE_MAPPING.get(genre.getId());
                if (mapped != null) {
                    return mapped;
                }
            }
        }
        return VideoCategory.ACTION; // default fallback
    }
}
