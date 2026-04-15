package hu.pogany.freshPotato.service;

import hu.pogany.freshPotato.dto.rate.GenericRateDto;
import hu.pogany.freshPotato.entity.Movie;
import hu.pogany.freshPotato.entity.Rate;
import hu.pogany.freshPotato.entity.RateId;
import hu.pogany.freshPotato.entity.User;
import hu.pogany.freshPotato.repository.MovieRepository;
import hu.pogany.freshPotato.repository.RateRepository;
import hu.pogany.freshPotato.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateServiceTest {

    @Mock
    private RateRepository rateRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private RateService rateService;

    @Test
    void saveRating_shouldThrowEntityNotFoundException_whenUserDoesNotExistForProvidedDto() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> rateService.saveRating(new GenericRateDto<>(1, 2, 5))
        );

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void saveRating_shouldThrowEntityNotFoundException_whenMovieDoesNotExistForProvidedDto() {
        when(userRepository.findById(1)).thenReturn(Optional.of(createUser(1)));
        when(movieRepository.findById(2)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> rateService.saveRating(new GenericRateDto<>(1, 2, 5))
        );

        assertEquals("Movie not found", exception.getMessage());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {-1, 0, 6})
    void saveRating_shouldThrowValidationException_whenRatingIsNullOrOutsideAllowedRange(Integer invalidRating) {
        when(userRepository.findById(1)).thenReturn(Optional.of(createUser(1)));
        when(movieRepository.findById(2)).thenReturn(Optional.of(createMovie(2)));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> rateService.saveRating(new GenericRateDto<>(1, 2, invalidRating))
        );

        assertEquals("Rate value must be between 1 and 5", exception.getMessage());
    }

    @Test
    void saveRating_shouldCreateAndPersistNewRateEntity_whenNoExistingRateFoundForUserAndMovie() {
        User user = createUser(1);
        Movie movie = createMovie(2);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(movieRepository.findById(2)).thenReturn(Optional.of(movie));
        when(rateRepository.findByUserIdAndMovieId(1, 2)).thenReturn(Optional.empty());

        rateService.saveRating(new GenericRateDto<>(1, 2, 4));

        ArgumentCaptor<Rate> captor = ArgumentCaptor.forClass(Rate.class);
        verify(rateRepository).save(captor.capture());
        Rate saved = captor.getValue();

        assertEquals(1, saved.getId().getUserId());
        assertEquals(2, saved.getId().getMovieId());
        assertEquals(Byte.valueOf((byte) 4), saved.getRating());
        assertEquals(user, saved.getUser());
        assertEquals(movie, saved.getMovie());
        assertNotNull(saved.getTime());
    }

    @Test
    void saveRating_shouldUpdateAndPersistExistingRateEntity_whenRateAlreadyExistsForUserAndMovie() {
        User user = createUser(1);
        Movie movie = createMovie(2);
        Rate existing = new Rate();
        existing.setId(createRateId(1, 2));
        existing.setRating((byte) 1);
        Instant previousTime = Instant.now().minusSeconds(10);
        existing.setTime(previousTime);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(movieRepository.findById(2)).thenReturn(Optional.of(movie));
        when(rateRepository.findByUserIdAndMovieId(1, 2)).thenReturn(Optional.of(existing));

        rateService.saveRating(new GenericRateDto<>(1, 2, 5));

        verify(rateRepository).save(existing);
        assertEquals(Byte.valueOf((byte) 5), existing.getRating());
        assertTrue(existing.getTime().isAfter(previousTime));
    }

    @Test
    void getAllByUser_shouldThrowEntityNotFoundException_whenUserIdDoesNotExist() {
        when(userRepository.findById(22)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> rateService.getAllByUser(22)
        );

        assertEquals("User doesn't exists", exception.getMessage());
    }

    @Test
    void getAllByUser_shouldReturnMappedRateDtos_whenUserExistsAndRatesArePresent() {
        User user = createUser(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        Rate rate = new Rate();
        rate.setId(createRateId(1, 2));
        rate.setRating((byte) 3);
        when(rateRepository.findByUser(user)).thenReturn(List.of(rate));

        List<GenericRateDto<Integer>> result = rateService.getAllByUser(1);

        assertEquals(1, result.size());
        assertEquals(1, result.getFirst().userId());
        assertEquals(2, result.getFirst().movieId());
        assertEquals(3, result.getFirst().rating());
    }

    @Test
    void deleteRating_shouldDelegateToRepositoryDeleteByUserAndMovieIds_whenDtoOrPrimitiveOverloadIsUsed() {
        rateService.deleteRating(3, 4);
        rateService.deleteRating(new hu.pogany.freshPotato.dto.rate.DeleteRateDto(5, 6));

        verify(rateRepository).deleteRateByUserIdAndMovieId(3, 4);
        verify(rateRepository).deleteRateByUserIdAndMovieId(5, 6);
    }

    private User createUser(int id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    private Movie createMovie(int id) {
        Movie movie = new Movie();
        movie.setId(id);
        return movie;
    }

    private RateId createRateId(int userId, int movieId) {
        RateId rateId = new RateId();
        rateId.setUserId(userId);
        rateId.setMovieId(movieId);
        return rateId;
    }
}

