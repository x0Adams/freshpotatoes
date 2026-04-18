package hu.pogany.freshPotato.service;

import hu.pogany.freshPotato.dto.response.MovieDto;
import hu.pogany.freshPotato.dto.response.SearchMovieDto;
import hu.pogany.freshPotato.entity.View;
import hu.pogany.freshPotato.mapper.Mapper;
import hu.pogany.freshPotato.repository.MovieRepository;
import hu.pogany.freshPotato.specification.MovieSpecification;
import hu.pogany.freshPotato.entity.Movie;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.NotContextException;
import javax.naming.TimeLimitExceededException;
import javax.security.auth.login.CredentialException;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class MovieService {
    private static final Logger log = LoggerFactory.getLogger(MovieService.class);
    private final MovieRepository movieRepository;
    private final Mapper mapper;
    private final PosterService posterService;
    private final TrailerService trailerService;

    public MovieService(MovieRepository movieRepository, Mapper mapper, PosterService posterService, TrailerService trailerService) {
        this.movieRepository = movieRepository;
        this.mapper = mapper;
        this.posterService = posterService;
        this.trailerService = trailerService;
    }

    public List<SearchMovieDto> searchForName(String name) {
        List<Movie> movies = movieRepository.findTop5ByNameIsLike(name + "%", JpaSort.unsafe("LENGTH(name)"));
        if (!movies.isEmpty())
            return mapper.toSearchMovieDtoList(movies);
        else
            throw new EntityNotFoundException("No movies found for name: " + name);
    }

    @Transactional(readOnly = false)
    public MovieDto getMovieSaveView(int movieId, int userId) {
        Movie movie = movieRepository.findById(movieId).orElseThrow(() -> new EntityNotFoundException("Movie not found"));
        fetchTrailerAndPoster(movie);
        View view = new View();
        view.setMovie(movie);
        view.setUserId(userId);
        movie.getViews().add(view);

        return mapper.toMovieDto(movie);
    }

    public MovieDto getMovie(int id) {
        Movie movie = movieRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Movie not found"));
        fetchTrailerAndPoster(movie);
        return mapper.toMovieDto(movie);
    }

    public List<SearchMovieDto> findPopularMovies(int page, int size) {
        List<Movie> movies = movieRepository.findByPopularity(PageRequest.of(page, size));
        return mapper.toSearchMovieDtoList(movies);
    }

    public List<SearchMovieDto> randomMovies() {
        return mapper.toSearchMovieDtoList(movieRepository.findRandom());
    }

    public List<SearchMovieDto> advancedSearch(String title, List<String> staff, List<String> genres, int page, int size) {
        if (title == null || title.trim().isEmpty()) {
            throw new ValidationException("Title must not be empty");
        }

        Specification<Movie> specification = Specification
                .where(MovieSpecification.titleExactOrPrefix(title))
                .and(MovieSpecification.hasStaff(staff))
                .and(MovieSpecification.hasGenres(genres))
                .and(MovieSpecification.orderByExactMatchThenPopularity(title));

        List<Movie> movies = movieRepository.findAll(specification, PageRequest.of(page, size)).getContent();
        if (movies.isEmpty()) {
            throw new EntityNotFoundException("No movies found for the given search criteria");
        }

        return mapper.toSearchMovieDtoList(movies);
    }

    private void fetchTrailerAndPoster(Movie movie) {
        fetchTrailer(movie);
        fetchPoster(movie);
    }

    private void fetchPoster(Movie movie) {
        if (movie.getWikipediaTitle() == null)
            return;

        if (!movie.getPosterPath().trim().equalsIgnoreCase("not fetched"))
            return;

        try {
            posterService.fetchPoster(movie);
        } catch (NotContextException | InterruptedException | TimeLimitExceededException | RuntimeException e) {
            log.error(e.getMessage());
        }
    }

    private void fetchTrailer(Movie movie) {
        if (!movie.getTrailer().trim().equalsIgnoreCase("not fetched"))
            return;

        try {
            trailerService.fetchTrailer(movie);
        } catch (NotContextException | CredentialException | TimeLimitExceededException | InterruptedException | RuntimeException e) {
            log.error(e.getMessage());
        }
    }
}
