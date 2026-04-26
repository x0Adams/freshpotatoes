package hu.pogany.freshPotato.service;

import hu.pogany.freshPotato.config.YoutubeConfig;
import hu.pogany.freshPotato.entity.Movie;
import hu.pogany.freshPotato.repository.MovieRepository;
import io.github.bucket4j.BlockingBucket;
import jakarta.persistence.EntityNotFoundException;
import org.apache.catalina.connector.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import javax.naming.NotContextException;
import javax.naming.TimeLimitExceededException;
import javax.security.auth.login.CredentialException;
import java.time.Duration;
import java.util.Optional;

@Service
public class TrailerService {
    private static final Logger log = LoggerFactory.getLogger(TrailerService.class);
    private final YoutubeConfig youtubeConfig;
    private final BlockingBucket bucket;
    private final MovieRepository movieRepository;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public TrailerService(YoutubeConfig youtubeConfig, @Qualifier("youtubeLimiter") BlockingBucket bucket, MovieRepository movieRepository, ObjectMapper objectMapper) {
        this.youtubeConfig = youtubeConfig;
        this.bucket = bucket;
        this.movieRepository = movieRepository;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.create(youtubeConfig.apiUrl());
    }

    @Transactional
    public void fetchTrailer(int movieId) throws EntityNotFoundException, NotContextException, InterruptedException, TimeLimitExceededException, CredentialException {
        Optional<Movie> optMovie = movieRepository.findById(movieId);

        fetchTrailer(optMovie.orElseThrow(() -> new EntityNotFoundException("no movie with this id")));
    }

    @Transactional
    public void fetchTrailer(Movie movie) throws NotContextException, InterruptedException, TimeLimitExceededException, CredentialException {
        validateTitle(movie);

        if (youtubeConfig.apiKey().equalsIgnoreCase("no"))
            return;

        if (!bucket.tryConsume(youtubeConfig.queryCost(), Duration.ofMillis(1)))
            throw new TimeLimitExceededException("out of daily quota");

        var response = fetchFromYoutube(movie);
        if (!validateResponseStatus(response.getStatusCode()))
            return;
        String trailer = getTrailerFromResponse(response.getBody());
        movie.setTrailer(trailer);

        movieRepository.save(movie);
        log.info("trailer is saved for {}", movie.getName());
    }

    private void validateTitle(Movie movie) throws NotContextException {
        if (movie.getName().isBlank()) {
            if (movie.getWikipediaTitle() == null) {
                throw new NotContextException("there is no relevant title for this movie");
            }
            movie.setName(movie.getWikipediaTitle());
        }
    }

    private ResponseEntity<String> fetchFromYoutube(Movie movie) {
        try {
            return restClient.get()
                    .uri(builder -> builder
                            .queryParam("part", "snippet")
                            .queryParam("type", "video")
                            .queryParam("maxResults", 1)
                            .queryParam("q", String.format("%s %s trailer", movie.getName(), getMovieReleaseYear(movie)))
                            .queryParam("relevanceLanguage", "en")
                            .queryParam("key", youtubeConfig.apiKey())
                            .build()
                    )
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(String.class);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatusCode.valueOf(403));
        }

    }

    private boolean validateResponseStatus(HttpStatusCode statusCode) throws CredentialException, InterruptedException, TimeLimitExceededException {
        if (statusCode.is2xxSuccessful())
            return true;

        int code = statusCode.value();
        switch (code) {
            case 400 -> throw new CredentialException("youtube request is invalid (bad request)");
            case 401 -> throw new CredentialException("youtube api key is missing or invalid");
            case 403 -> {
                while (bucket.tryConsume(1, Duration.ZERO));
                throw new TimeLimitExceededException("youtube daily quota exceeded");
            }
            case 404 -> throw new EntityNotFoundException("youtube endpoint not found");
            case 429 -> throw new TimeLimitExceededException("too many requests to youtube");
            case 500, 502, 503, 504 -> throw new IllegalStateException("youtube service is temporarily unavailable");
            default -> throw new IllegalStateException("unexpected youtube response status: " + code);
        }
    }

    private String getTrailerFromResponse(String body) {
        JsonNode root = objectMapper.readTree(body);
        JsonNode items = root.get("items");

        return items.get(0).get("id").get("videoId").asString();
    }

    private String getMovieReleaseYear(Movie movie) {
        try {
            return "" + movie.getReleaseDate().getYear();
        } catch (RuntimeException e) {
            return "";
        }
    }



}
