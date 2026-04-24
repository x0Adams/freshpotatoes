package hu.pogany.freshPotato.service;

import hu.pogany.freshPotato.dto.response.SearchMovieDto;
import hu.pogany.freshPotato.entity.Movie;
import hu.pogany.freshPotato.mapper.Mapper;
import hu.pogany.freshPotato.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private Mapper mapper;

    @Mock
    private PosterService posterService;

    @Mock
    private TrailerService trailerService;

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private MovieInPlaylistRepository movieInPlaylistRepository;

    @Mock
    private ProductionsCountryRepository productionsCountryRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private RateRepository rateRepository;

    @Mock
    private StaffRoleInMovieRepository staffRoleInMovieRepository;

    @InjectMocks
    private MovieService movieService;

    @Test
    void advancedSearch_shouldThrowValidationException_whenTitleIsNullOrBlankBecauseExactAndPrefixMatchingNeedsATitle() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> movieService.advancedSearch("  ", List.of("Keanu Reeves"), List.of("Action"), 0, 10)
        );

        assertEquals("Title must not be empty", exception.getMessage());
    }

    @Test
    void advancedSearch_shouldThrowEntityNotFoundException_whenRepositoryReturnsNoMovieForGivenFilters() {
        when(movieRepository.findAll(org.mockito.ArgumentMatchers.<Specification<Movie>>any(), eq(PageRequest.of(0, 10))))
                .thenReturn(new PageImpl<>(List.of()));

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> movieService.advancedSearch("Matrix", List.of("Keanu Reeves"), List.of("Action"), 0, 10)
        );

        assertEquals("No movies found for the given search criteria", exception.getMessage());
    }

    @Test
    void advancedSearch_shouldReturnMappedDtos_whenRepositoryFindsMoviesOrderedBySpecification() {
        Movie movie = new Movie();
        movie.setId(603);
        movie.setName("The Matrix");

        SearchMovieDto expectedDto = new SearchMovieDto("603", "The Matrix", null, null, null);

        when(movieRepository.findAll(org.mockito.ArgumentMatchers.<Specification<Movie>>any(), eq(PageRequest.of(0, 10))))
                .thenReturn(new PageImpl<>(List.of(movie)));
        when(mapper.toSearchMovieDtoList(List.of(movie))).thenReturn(List.of(expectedDto));

        List<SearchMovieDto> result = movieService.advancedSearch(
                "The Matrix",
                List.of("Keanu Reeves"),
                List.of("Action"),
                0,
                10
        );

        assertEquals(1, result.size());
        assertEquals("603", result.getFirst().id());
        verify(mapper).toSearchMovieDtoList(List.of(movie));
    }

    @Test
    void deleteMovie_shouldThrowEntityNotFoundException_whenMovieDoesNotExist() {
        when(movieRepository.findById(404)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> movieService.deleteMovie(404)
        );

        assertEquals("Movie not found", exception.getMessage());
        verify(movieRepository, never()).delete(any(Movie.class));
    }

    @Test
    void deleteMovie_shouldDeleteAllRelationsBeforeDeletingMovie() {
        int movieId = 603;
        Movie movie = new Movie();
        movie.setId(movieId);

        when(movieRepository.findById(movieId)).thenReturn(Optional.of(movie));

        movieService.deleteMovie(movieId);

        InOrder inOrder = inOrder(
                movieInPlaylistRepository,
                productionsCountryRepository,
                reviewRepository,
                rateRepository,
                staffRoleInMovieRepository,
                movieRepository
        );

        inOrder.verify(movieInPlaylistRepository).deleteByMovieId(movieId);
        inOrder.verify(productionsCountryRepository).deleteByMovie(movie);
        inOrder.verify(reviewRepository).deleteByMovie(movie);
        inOrder.verify(rateRepository).deleteByMovie(movie);
        inOrder.verify(staffRoleInMovieRepository).deleteByMovie(movie);
        inOrder.verify(movieRepository).delete(any(Movie.class));
    }
}
