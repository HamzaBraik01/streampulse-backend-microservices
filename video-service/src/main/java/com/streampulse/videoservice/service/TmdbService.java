package com.streampulse.videoservice.service;

import com.streampulse.videoservice.dto.TmdbMovieDTO;
import com.streampulse.videoservice.dto.VideoDTO;

import java.util.List;

/**
 * Service interface for TMDb API integration.
 * Handles searching TMDb for movie metadata and importing it into the local database.
 */
public interface TmdbService {

    /**
     * Search TMDb for movies matching the given title.
     *
     * @param title the movie title to search for
     * @return list of matching TMDb movie results
     */
    List<TmdbMovieDTO> searchMovies(String title);

    /**
     * Import a movie from TMDb by its TMDb ID.
     * Fetches full details (title, description, cast, director, trailer, poster)
     * and creates a local Video entry.
     *
     * @param tmdbId the TMDb movie ID
     * @return the created VideoDTO
     */
    VideoDTO importMovie(Long tmdbId);
}
