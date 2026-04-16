package hu.pogany.freshPotato.service;

import hu.pogany.freshPotato.config.WikiConfig;
import hu.pogany.freshPotato.entity.Movie;
import hu.pogany.freshPotato.entity.User;
import hu.pogany.freshPotato.repository.MovieRepository;
import hu.pogany.freshPotato.repository.UserRepository;
import io.github.bucket4j.BlockingBucket;
import io.swagger.v3.core.util.Json;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import javax.naming.NotContextException;
import javax.naming.TimeLimitExceededException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PosterService {

    private static final Logger log = LoggerFactory.getLogger(PosterService.class);
    private final BlockingBucket bucket;
    private final MovieRepository movieRepository;
    private final WikiConfig wikiConfig;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public PosterService(BlockingBucket bucket, MovieRepository movieRepository, WikiConfig wikiConfig, ObjectMapper objectMapper) {
        this.bucket = bucket;
        this.movieRepository = movieRepository;
        this.wikiConfig = wikiConfig;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.create(wikiConfig.apiUrl());
    }

    @Transactional
    public void fetchPoster(int movieId) throws NotContextException, TimeLimitExceededException, InterruptedException {
        Optional<Movie> movieOptional = movieRepository.findById(movieId);

        fetchPoster(movieOptional.orElseThrow(() -> new EntityNotFoundException("No movie with this id")));
    }

    @Transactional
    public void fetchPoster(Movie movie) throws NotContextException, InterruptedException, TimeLimitExceededException {
        if (movie.getWikipediaTitle() == null || movie.getWikipediaTitle().isBlank())
            throw new NotContextException("There is no Wikipedia title for this movie");

        if (!bucket.tryConsume(1, Duration.ofSeconds(1)))
            throw new TimeLimitExceededException("Too many requests");

        String response = fetchFromWikipedia(movie.getWikipediaTitle());
        String path = getLargestScalePath(getPosterNode(response));
        path = path.substring(2);

        movie.setPosterPath(path);

        movieRepository.save(movie);

        log.info("poster was saved for {}", movie.getName());

    }

    private String fetchFromWikipedia(String title) {
        return restClient.get()
                .uri("/{title}", title)
                .accept(MediaType.APPLICATION_JSON)
                .header("User-Agent", wikiConfig.userAgent())
                .header("Contact", wikiConfig.contactEmail())
                .retrieve()
                .body(String.class);
    }

    private JsonNode getPosterNode(String content) {
        JsonNode root = objectMapper.readTree(content);
        JsonNode items = root.get("items");

        for (JsonNode node : items) {
            if (isPosterNode(node))
                return node;
        }

        return items.get(0);
    }

    private String getLargestScalePath(JsonNode posterNode) {
        JsonNode srcset = posterNode.get("srcset");
        JsonNode last = srcset.get(srcset.size() - 1);

        return last.get("src").asString();
    }

    private boolean isPosterNode(JsonNode node) {
        return node.get("title").asString().contains("poster") && node.get("type").asString().equals("image");
    }


}
