package com.streampulse.videoservice.service;

import com.streampulse.videoservice.config.TmdbConfig;
import com.streampulse.videoservice.dto.*;
import com.streampulse.videoservice.entity.Video;
import com.streampulse.videoservice.entity.VideoCategory;
import com.streampulse.videoservice.entity.VideoType;
import com.streampulse.videoservice.mapper.VideoMapper;
import com.streampulse.videoservice.repository.VideoRepository;
import com.streampulse.videoservice.service.impl.TmdbServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TmdbServiceImpl — tests TMDb API integration logic in isolation.
 */
@ExtendWith(MockitoExtension.class)
class TmdbServiceImplTest {

    @Mock
    private WebClient tmdbWebClient;

    @Mock
    private TmdbConfig tmdbConfig;

    @Mock
    private VideoRepository videoRepository;

    @Mock
    private VideoMapper videoMapper;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private TmdbServiceImpl tmdbService;

    @BeforeEach
    void setUp() {
        // Common setup if needed
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // searchMovies
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("searchMovies - should return empty list when API key is null")
    void searchMovies_ShouldReturnEmptyWhenApiKeyNull() {
        when(tmdbConfig.getApiKey()).thenReturn(null);

        List<TmdbMovieDTO> result = tmdbService.searchMovies("Inception");

        assertThat(result).isEmpty();
        verifyNoInteractions(tmdbWebClient);
    }

    @Test
    @DisplayName("searchMovies - should return empty list when API key is empty")
    void searchMovies_ShouldReturnEmptyWhenApiKeyEmpty() {
        when(tmdbConfig.getApiKey()).thenReturn("");

        List<TmdbMovieDTO> result = tmdbService.searchMovies("Inception");

        assertThat(result).isEmpty();
        verifyNoInteractions(tmdbWebClient);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("searchMovies - should return results from TMDb API")
    void searchMovies_ShouldReturnResults() {
        when(tmdbConfig.getApiKey()).thenReturn("test-api-key");

        TmdbMovieDTO movie = TmdbMovieDTO.builder()
                .id(27205L)
                .title("Inception")
                .overview("A mind-bending thriller")
                .voteAverage(8.8)
                .build();

        TmdbSearchResponse response = new TmdbSearchResponse();
        response.setResults(List.of(movie));
        response.setTotalResults(1);

        when(tmdbWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(TmdbSearchResponse.class)).thenReturn(Mono.just(response));

        List<TmdbMovieDTO> result = tmdbService.searchMovies("Inception");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Inception");
        assertThat(result.get(0).getId()).isEqualTo(27205L);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("searchMovies - should return empty list when TMDb returns null response")
    void searchMovies_ShouldReturnEmptyWhenNullResponse() {
        when(tmdbConfig.getApiKey()).thenReturn("test-api-key");

        when(tmdbWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(TmdbSearchResponse.class)).thenReturn(Mono.empty());

        List<TmdbMovieDTO> result = tmdbService.searchMovies("Unknown Movie");

        assertThat(result).isEmpty();
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("searchMovies - should handle API error gracefully")
    void searchMovies_ShouldHandleApiError() {
        when(tmdbConfig.getApiKey()).thenReturn("test-api-key");

        when(tmdbWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(TmdbSearchResponse.class))
                .thenReturn(Mono.error(new RuntimeException("API Error")));

        List<TmdbMovieDTO> result = tmdbService.searchMovies("Test");

        assertThat(result).isEmpty();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // importMovie
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("importMovie - should throw when API key is null")
    void importMovie_ShouldThrowWhenApiKeyNull() {
        when(tmdbConfig.getApiKey()).thenReturn(null);

        assertThatThrownBy(() -> tmdbService.importMovie(27205L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("TMDb API key is not configured");
    }

    @Test
    @DisplayName("importMovie - should throw when API key is empty")
    void importMovie_ShouldThrowWhenApiKeyEmpty() {
        when(tmdbConfig.getApiKey()).thenReturn("");

        assertThatThrownBy(() -> tmdbService.importMovie(27205L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("TMDb API key is not configured");
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("importMovie - should import movie and save to database")
    void importMovie_ShouldImportAndSave() {
        when(tmdbConfig.getApiKey()).thenReturn("test-api-key");

        TmdbMovieDetailDTO.Genre actionGenre = new TmdbMovieDetailDTO.Genre(28, "Action");
        TmdbMovieDetailDTO.CastMember cast1 = new TmdbMovieDetailDTO.CastMember("Leonardo DiCaprio", "Cobb");
        TmdbMovieDetailDTO.CrewMember director = new TmdbMovieDetailDTO.CrewMember("Christopher Nolan", "Director");
        TmdbMovieDetailDTO.Credits credits = new TmdbMovieDetailDTO.Credits(List.of(cast1), List.of(director));
        TmdbMovieDetailDTO.VideoResult trailer = new TmdbMovieDetailDTO.VideoResult("abc123", "YouTube", "Trailer");
        TmdbMovieDetailDTO.Videos videos = new TmdbMovieDetailDTO.Videos(List.of(trailer));

        TmdbMovieDetailDTO detail = TmdbMovieDetailDTO.builder()
                .id(27205L)
                .title("Inception")
                .overview("A mind-bending thriller")
                .posterPath("/poster.jpg")
                .releaseDate("2010-07-16")
                .voteAverage(8.8)
                .runtime(148)
                .genres(List.of(actionGenre))
                .credits(credits)
                .videos(videos)
                .build();

        Video savedVideo = Video.builder()
                .id(1L)
                .title("Inception")
                .description("A mind-bending thriller")
                .type(VideoType.FILM)
                .category(VideoCategory.ACTION)
                .build();

        VideoDTO expectedDTO = VideoDTO.builder()
                .id(1L)
                .title("Inception")
                .type(VideoType.FILM)
                .category(VideoCategory.ACTION)
                .build();

        when(tmdbWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(TmdbMovieDetailDTO.class)).thenReturn(Mono.just(detail));
        when(videoRepository.save(any(Video.class))).thenReturn(savedVideo);
        when(videoMapper.toDTO(savedVideo)).thenReturn(expectedDTO);

        VideoDTO result = tmdbService.importMovie(27205L);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Inception");
        assertThat(result.getCategory()).isEqualTo(VideoCategory.ACTION);
        verify(videoRepository).save(any(Video.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("importMovie - should throw when TMDb returns null")
    void importMovie_ShouldThrowWhenNullResponse() {
        when(tmdbConfig.getApiKey()).thenReturn("test-api-key");

        when(tmdbWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(TmdbMovieDetailDTO.class)).thenReturn(Mono.empty());

        assertThatThrownBy(() -> tmdbService.importMovie(99999L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No data returned from TMDb");
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("importMovie - should map SCIENCE_FICTION genre correctly")
    void importMovie_ShouldMapSciFiGenre() {
        when(tmdbConfig.getApiKey()).thenReturn("test-api-key");

        TmdbMovieDetailDTO.Genre sciFiGenre = new TmdbMovieDetailDTO.Genre(878, "Science Fiction");
        TmdbMovieDetailDTO detail = TmdbMovieDetailDTO.builder()
                .id(1L)
                .title("Interstellar")
                .overview("Space exploration")
                .releaseDate("2014-11-07")
                .runtime(169)
                .genres(List.of(sciFiGenre))
                .build();

        Video savedVideo = Video.builder().id(1L).title("Interstellar").category(VideoCategory.SCIENCE_FICTION).build();
        VideoDTO expectedDTO = VideoDTO.builder().id(1L).title("Interstellar").category(VideoCategory.SCIENCE_FICTION).build();

        when(tmdbWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(TmdbMovieDetailDTO.class)).thenReturn(Mono.just(detail));
        when(videoRepository.save(any(Video.class))).thenReturn(savedVideo);
        when(videoMapper.toDTO(savedVideo)).thenReturn(expectedDTO);

        VideoDTO result = tmdbService.importMovie(1L);

        assertThat(result.getCategory()).isEqualTo(VideoCategory.SCIENCE_FICTION);
    }
}
