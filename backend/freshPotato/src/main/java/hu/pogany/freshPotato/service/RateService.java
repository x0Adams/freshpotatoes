package hu.pogany.freshPotato.service;

import hu.pogany.freshPotato.dto.rate.GenericRateDto;
import hu.pogany.freshPotato.entity.*;
import hu.pogany.freshPotato.repository.MovieRepository;
import hu.pogany.freshPotato.repository.RateRepository;
import hu.pogany.freshPotato.repository.UserRepository;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class RateService extends AbstractRateService<Integer, Rate> {

    private final MovieService movieService;

    public RateService(RateRepository rateRepository, UserRepository userRepository, MovieRepository movieRepository, MovieService movieService) {
        super(rateRepository, userRepository, movieRepository);
        this.movieService = movieService;
    }

    @Override
    protected Rate createEntity(User user, Movie movie, Integer rating) {
        Rate rate = new Rate();
        RateId id = new RateId();
        id.setUserId(user.getId());
        id.setMovieId(movie.getId());

        rate.setId(id);
        rate.setUser(user);
        rate.setMovie(movie);
        rate.setRating(rating.byteValue());
        rate.setTime(Instant.now());
        return rate;
    }

    @Override
    protected void updateEntity(Rate entity, Integer rating) {
        entity.setRating(rating.byteValue());
        entity.setTime(Instant.now());
    }

    @Override
    protected void validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new ValidationException("Rate value must be between 1 and 5");
        }
    }

    @Override
    protected List<GenericRateDto<Integer>> mapToDto(List<Rate> entities) {
        return entities.stream().map(it -> {
                    var movie = movieService.getMovie(it.getId().getMovieId());
                    return GenericRateDto
                            .<Integer>builder()
                            .movieId(movie.id())
                            .userId(it.getId().getUserId())
                            .rating(Integer.valueOf(it.getRating()))
                            .name(movie.name())
                            .posterPath(movie.posterPath())
                            .releaseDate(movie.releaseDate())
                            .genres(movie.genres())
                            .username(it.getUser().getUsername())
                            .build();
                }
        ).toList();
    }
}
