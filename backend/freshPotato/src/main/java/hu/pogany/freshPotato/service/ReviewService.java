package hu.pogany.freshPotato.service;

import hu.pogany.freshPotato.dto.rate.GenericRateDto;
import hu.pogany.freshPotato.entity.Movie;
import hu.pogany.freshPotato.entity.RateId;
import hu.pogany.freshPotato.entity.Review;
import hu.pogany.freshPotato.entity.User;
import hu.pogany.freshPotato.repository.MovieRepository;
import hu.pogany.freshPotato.repository.ReviewRepository;
import hu.pogany.freshPotato.repository.UserRepository;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ReviewService extends AbstractRateService<String, Review> {
    public ReviewService(ReviewRepository rateRepository, UserRepository userRepository, MovieRepository movieRepository) {
        super(rateRepository, userRepository, movieRepository);
    }

    @Override
    protected Review createEntity(User user, Movie movie, String rating) {
        Review review = new Review();
        RateId id = new RateId();
        id.setUserId(user.getId());
        id.setMovieId(movie.getId());

        review.setId(id);
        review.setUser(user);
        review.setMovie(movie);
        review.setReview(rating);
        review.setTime(Instant.now());
        return review;
    }

    @Override
    protected void updateEntity(Review entity, String rating) {
        entity.setReview(rating);
        entity.setTime(Instant.now());
    }

    @Override
    protected void validateRating(String rating) {
        if (rating == null || rating.isBlank()) {
            throw new ValidationException("Review text must not be blank");
        }

        if (rating.length() > 4000) {
            throw new ValidationException("Review text must be at most 4000 characters");
        }
    }

    @Override
    protected List<GenericRateDto<String>> mapToDto(List<Review> entities) {
        return entities.stream().map(it -> GenericRateDto
                .<String>builder()
                .movieId(it.getId().getMovieId())
                .userId(it.getId().getUserId())
                .rating(it.getReview())
                .build()
        ).toList();
    }


}
