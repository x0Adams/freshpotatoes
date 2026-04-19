package hu.pogany.freshPotato.service;

import hu.pogany.freshPotato.dto.response.SearchMovieDto;
import hu.pogany.freshPotato.entity.Movie;
import hu.pogany.freshPotato.mapper.Mapper;
import hu.pogany.freshPotato.repository.MovieRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private Mapper mapper;

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
}

