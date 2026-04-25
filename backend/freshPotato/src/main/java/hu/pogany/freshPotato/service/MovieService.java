package hu.pogany.freshPotato.service;

import hu.pogany.freshPotato.dto.ModifyMovieDto;
import hu.pogany.freshPotato.dto.response.MovieDto;
import hu.pogany.freshPotato.dto.response.SearchMovieDto;
import hu.pogany.freshPotato.entity.View;
import hu.pogany.freshPotato.mapper.Mapper;
import hu.pogany.freshPotato.repository.*;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class MovieService {
    private static final Logger log = LoggerFactory.getLogger(MovieService.class);
    private final MovieRepository movieRepository;
    private final Mapper mapper;
    private final PosterService posterService;
    private final TrailerService trailerService;
    private final GenreRepository genreRepository;
    private final MovieInPlaylistRepository movieInPlaylistRepository;
    private final ProductionsCountryRepository productionsCountryRepository;
    private final ReviewRepository reviewRepository;
    private final RateRepository rateRepository;
    private final StaffRoleInMovieRepository staffRoleInMovieRepository;

    public MovieService(MovieRepository movieRepository,
                        Mapper mapper,
                        PosterService posterService,
                        TrailerService trailerService,
                        GenreRepository genreRepository,
                        MovieInPlaylistRepository movieInPlaylistRepository,
                        ProductionsCountryRepository productionsCountryRepository,
                        ReviewRepository reviewRepository,
                        RateRepository rateRepository,
                        StaffRoleInMovieRepository staffRoleInMovieRepository) {
        this.movieRepository = movieRepository;
        this.mapper = mapper;
        this.posterService = posterService;
        this.trailerService = trailerService;
        this.genreRepository = genreRepository;
        this.movieInPlaylistRepository = movieInPlaylistRepository;
        this.productionsCountryRepository = productionsCountryRepository;
        this.reviewRepository = reviewRepository;
        this.rateRepository = rateRepository;
        this.staffRoleInMovieRepository = staffRoleInMovieRepository;
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

    @Transactional(readOnly = false)
    public MovieDto getMovie(int id) {
        Movie movie = movieRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Movie not found"));
        fetchTrailerAndPoster(movie);
        return mapper.toMovieDto(movie);
    }

    @Transactional
    public void modify(int id, ModifyMovieDto dto) {
        Movie movie = movieRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Movie not found"));
        movie.setName(dto.name());
        movie.setPosterPath(dto.posterPath());
        movie.setDuration(dto.duration());
        movie.setReleaseDate(dto.releaseDate());
        movie.setWikipediaTitle(dto.wikipediaTitle());
        movie.setYoutubeMovie(dto.youtubeMovie());
        movie.setTrailer(dto.trailer());
    }

    @Transactional
    public void deleteMovie(int movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new EntityNotFoundException("Movie not found"));

        // Clear local collections to prevent Hibernate from trying to "double-delete" or sync them
        movie.getMovieInPlaylists().clear();
        movie.getCountries().clear();
        movie.getRates().clear();
        movie.getReviews().clear();
        movie.getStaffRoleInMovies().clear();
        movie.getGenres().clear();
        if (movie.getViews() != null) movie.getViews().clear();

        // Remove dependencies explicitly from DB
        movieInPlaylistRepository.deleteByMovieId(movieId);
        productionsCountryRepository.deleteByMovie(movie);
        reviewRepository.deleteByMovie(movie);
        rateRepository.deleteByMovie(movie);
        staffRoleInMovieRepository.deleteByMovie(movie);

        movieRepository.delete(movie);
    }

    public MovieDto getMovieNotFetchExternal(int id) {
        Movie movie = movieRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Movie not found"));
        return mapper.toMovieDto(movie);
    }

    public List<SearchMovieDto> findPopularMovies(int page, int size) {
        long totalStartedAt = System.nanoTime();

        long queryStartedAt = System.nanoTime();
        List<Integer> movieIds = movieRepository.findPopularMovieIds(PageRequest.of(page, size));
        List<Movie> movies = movieIds.isEmpty() ? List.of() : movieRepository.findByIdIn(movieIds);
        long queryMs = (System.nanoTime() - queryStartedAt) / 1_000_000;

        sortById(movieIds, movies);

        long mappingStartedAt = System.nanoTime();
        List<SearchMovieDto> result = mapper.toSearchMovieDtoList(movies);
        long mappingMs = (System.nanoTime() - mappingStartedAt) / 1_000_000;
        long totalMs = (System.nanoTime() - totalStartedAt) / 1_000_000;

        log.debug("findPopularMovies took {} ms (query={} ms, mapping={} ms, page={}, size={}, rows={})",
                totalMs, queryMs, mappingMs, page, size, result.size());
        return result;
    }

    public List<SearchMovieDto> findPopularMoviesByGenre(String genre, int page, int size) {
        if (!genreRepository.existsByName(genre))
            throw new EntityNotFoundException("Genre doesn't exist in database");
        List<Integer> movieIds = movieRepository.findPopularMovieIdsByGenre(genre, PageRequest.of(page, size));

        if (movieIds.isEmpty())
            throw new EntityNotFoundException("No movies by this genre");

        List<Movie> movies = movieRepository.findByIdIn(movieIds);
        sortById(movieIds, movies);

        return mapper.toSearchMovieDtoList(movies);
    }

    public List<SearchMovieDto> randomMovies() {
        long totalStartedAt = System.nanoTime();

        long queryStartedAt = System.nanoTime();
        List<Movie> movies = movieRepository.findRandom();
        long queryMs = (System.nanoTime() - queryStartedAt) / 1_000_000;

        long mappingStartedAt = System.nanoTime();
        List<SearchMovieDto> result = mapper.toSearchMovieDtoList(movies);
        long mappingMs = (System.nanoTime() - mappingStartedAt) / 1_000_000;
        long totalMs = (System.nanoTime() - totalStartedAt) / 1_000_000;

        log.debug("randomMovies took {} ms (query={} ms, mapping={} ms, rows={})", totalMs, queryMs, mappingMs, result.size());
        return result;
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

    private void sortById(List<Integer> movieIds, List<Movie> movies) {
        if (!movieIds.isEmpty()) {
            Map<Integer, Integer> positionById = new HashMap<>(movieIds.size());
            for (int i = 0; i < movieIds.size(); i++) {
                positionById.put(movieIds.get(i), i);
            }
            movies.sort(Comparator.comparingInt(movie -> positionById.getOrDefault(movie.getId(), Integer.MAX_VALUE)));
        }
    }
}
