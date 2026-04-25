package hu.pogany.freshPotato.service;

import hu.pogany.freshPotato.config.RecommendationConfig;
import hu.pogany.freshPotato.dto.recommendation.RecommendationResponseDto;
import hu.pogany.freshPotato.dto.response.SearchMovieDto;
import hu.pogany.freshPotato.entity.Movie;
import hu.pogany.freshPotato.mapper.Mapper;
import hu.pogany.freshPotato.repository.MovieRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);
    private static final String NO_INTERACTION_MESSAGE = "No previous interaction for recommending movies";
    private static final String SERVICE_UNAVAILABLE_MESSAGE = "Recommendation service unavailable";
    private static final String INVALID_RECOMMENDATION_REQUEST_MESSAGE = "Recommendation request failed";

    private final RecommendationConfig recommendationConfig;
    private final MovieRepository movieRepository;
    private final Mapper mapper;
    private final RestClient restClient;

    public RecommendationService(RecommendationConfig recommendationConfig,
                                 MovieRepository movieRepository,
                                 Mapper mapper,
                                 RestClient.Builder restClientBuilder) {
        this.recommendationConfig = recommendationConfig;
        this.movieRepository = movieRepository;
        this.mapper = mapper;
        this.restClient = restClientBuilder.baseUrl(recommendationConfig.apiUrl()).build();
    }

    public List<SearchMovieDto> getRecommendations(int userId) {
        List<Integer> movieIds = fetchRecommendedMovieIds(userId);

        if (movieIds.isEmpty()) {
            // Commented out to prevent 404/500 errors when service is down or no data
            // throw new EntityNotFoundException(NO_INTERACTION_MESSAGE);
            return Collections.emptyList();
        }

        List<Movie> movies = movieRepository.findByIdIn(movieIds);
        if (movies.isEmpty()) {
            // throw new EntityNotFoundException(NO_INTERACTION_MESSAGE);
            return Collections.emptyList();
        }

        Map<Integer, Movie> moviesById = new LinkedHashMap<>();
        for (Movie movie : movies) {
            moviesById.put(movie.getId(), movie);
        }

        return movieIds.stream()
                .map(moviesById::get)
                .filter(Objects::nonNull)
                .map(mapper::toSearchMovieDto)
                .toList();
    }

    private List<Integer> fetchRecommendedMovieIds(int userId) {
        /* 
        // --- OLD IMPLEMENTATION (Throws 500 when service is unreachable) ---
        RecommendationResponseDto body;

        try {
            body = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/recommend/{userId}")
                            .queryParam("count", recommendationConfig.defaultCount())
                            .build(userId)
                    )
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(RecommendationResponseDto.class);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new EntityNotFoundException(NO_INTERACTION_MESSAGE);
            }

            if (ex.getStatusCode().is4xxClientError()) {
                throw new ValidationException(INVALID_RECOMMENDATION_REQUEST_MESSAGE + ": HTTP " + ex.getStatusCode().value());
            }

            throw new IllegalStateException(SERVICE_UNAVAILABLE_MESSAGE);
        } catch (RestClientException ex) {
            throw new IllegalStateException(SERVICE_UNAVAILABLE_MESSAGE);
        }

        if (body == null || body.movie_ids() == null) {
            return Collections.emptyList();
        }

        return body.movie_ids().stream()
                .filter(Objects::nonNull)
                .toList();
        */

        // --- NEW IMPLEMENTATION (Safe Mode: Graceful failure) ---
        try {
            RecommendationResponseDto body = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/recommend/{userId}")
                            .queryParam("count", recommendationConfig.defaultCount())
                            .build(userId)
                    )
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(RecommendationResponseDto.class);

            if (body == null || body.movie_ids() == null) {
                return Collections.emptyList();
            }

            return body.movie_ids().stream()
                    .filter(Objects::nonNull)
                    .toList();
        } catch (Exception ex) {
            // Log the failure in the Java console for your awareness
            log.error("Recommendation microservice unreachable or returned error: {}", ex.getMessage());
            // Return empty list so the platform stays functional
            return Collections.emptyList();
        }
    }
}
