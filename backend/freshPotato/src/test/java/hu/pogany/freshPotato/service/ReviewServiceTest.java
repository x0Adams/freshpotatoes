package hu.pogany.freshPotato.service;

import hu.pogany.freshPotato.dto.rate.GenericRateDto;
import hu.pogany.freshPotato.entity.Genre;
import hu.pogany.freshPotato.entity.Movie;
import hu.pogany.freshPotato.entity.RateId;
import hu.pogany.freshPotato.entity.Review;
import hu.pogany.freshPotato.entity.User;
import hu.pogany.freshPotato.repository.MovieRepository;
import hu.pogany.freshPotato.repository.ReviewRepository;
import hu.pogany.freshPotato.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void saveRating_shouldThrowValidationException_whenReviewTextIsNullBlankOrTooLong() {
        when(userRepository.findById(1)).thenReturn(Optional.of(createUser(1)));
        when(movieRepository.findById(2)).thenReturn(Optional.of(createMovie(2)));

        assertThrows(ValidationException.class, () -> reviewService.saveRating(createRateDto(1, 2, null)));
        assertThrows(ValidationException.class, () -> reviewService.saveRating(createRateDto(1, 2, "   ")));
        assertThrows(ValidationException.class, () -> reviewService.saveRating(createRateDto(1, 2, "a".repeat(4001))));
    }

    @Test
    void saveRating_shouldCreateAndPersistNewReviewEntity_whenNoExistingReviewFoundForUserAndMovie() {
        User user = createUser(1);
        Movie movie = createMovie(2);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(movieRepository.findById(2)).thenReturn(Optional.of(movie));
        when(reviewRepository.findByUserIdAndMovieId(1, 2)).thenReturn(Optional.empty());

        reviewService.saveRating(createRateDto(1, 2, "great movie"));

        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(captor.capture());
        Review saved = captor.getValue();

        assertEquals(1, saved.getId().getUserId());
        assertEquals(2, saved.getId().getMovieId());
        assertEquals("great movie", saved.getReview());
        assertEquals(user, saved.getUser());
        assertEquals(movie, saved.getMovie());
        assertNotNull(saved.getTime());
    }

    @Test
    void saveRating_shouldUpdateAndPersistExistingReviewEntity_whenReviewAlreadyExistsForUserAndMovie() {
        User user = createUser(1);
        Movie movie = createMovie(2);
        Review existing = new Review();
        existing.setId(createRateId(1, 2));
        existing.setReview("old review");
        Instant previousTime = Instant.now().minusSeconds(5);
        existing.setTime(previousTime);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(movieRepository.findById(2)).thenReturn(Optional.of(movie));
        when(reviewRepository.findByUserIdAndMovieId(1, 2)).thenReturn(Optional.of(existing));

        reviewService.saveRating(createRateDto(1, 2, "updated review"));

        verify(reviewRepository).save(existing);
        assertEquals("updated review", existing.getReview());
        assertTrue(existing.getTime().isAfter(previousTime));
    }

    @Test
    void getAllByMovie_shouldThrowEntityNotFoundException_whenMovieIdDoesNotExist() {
        when(movieRepository.existsById(99)).thenReturn(false);

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> reviewService.getAllByMovie(99)
        );

        assertEquals("no movie with this id in the database", exception.getMessage());
    }

    @Test
    void getAllByMovie_shouldReturnMappedReviewDtos_whenMovieExistsAndReviewsArePresent() {
        Movie movie = createMovie(2);
        movie.setName("Movie 2");
        movie.setPosterPath("poster.jpg");
        Genre genre = new Genre();
        genre.setName("Drama");
        movie.setGenres(new LinkedHashSet<>(List.of(genre)));
        when(movieRepository.existsById(2)).thenReturn(true);

        Review review = new Review();
        review.setId(createRateId(1, 2));
        review.setReview("solid");
        User user = createUser(1);
        user.setUsername("tester");
        review.setUser(user);
        review.setMovie(movie);
        when(reviewRepository.findByMovieId(2)).thenReturn(List.of(review));

        List<GenericRateDto<String>> result = reviewService.getAllByMovie(2);

        assertEquals(1, result.size());
        assertEquals(1, result.getFirst().getUserId());
        assertEquals(2, result.getFirst().getMovieId());
        assertEquals("solid", result.getFirst().getRating());
        assertEquals("tester", result.getFirst().getUsername());
        assertEquals("Movie 2", result.getFirst().getName());
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

    private GenericRateDto<String> createRateDto(int userId, int movieId, String rating) {
        return GenericRateDto.<String>builder()
                .userId(userId)
                .movieId(movieId)
                .rating(rating)
                .build();
    }

}

