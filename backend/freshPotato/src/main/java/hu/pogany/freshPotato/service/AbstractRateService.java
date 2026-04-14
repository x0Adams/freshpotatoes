package hu.pogany.freshPotato.service;

import hu.pogany.freshPotato.dto.rate.DeleteRateDto;
import hu.pogany.freshPotato.dto.rate.GenericRateDto;
import hu.pogany.freshPotato.entity.Movie;
import hu.pogany.freshPotato.entity.User;
import hu.pogany.freshPotato.repository.GenericRateRepository;
import hu.pogany.freshPotato.repository.MovieRepository;
import hu.pogany.freshPotato.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public abstract class AbstractRateService<T, S> {
    private final GenericRateRepository<S> rateRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;

    public AbstractRateService(GenericRateRepository<S> rateRepository, UserRepository userRepository, MovieRepository movieRepository) {
        this.rateRepository = rateRepository;
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
    }

    @Transactional
    public void saveRating(@Valid GenericRateDto<T> genericRateDto) {
        User user = userRepository
                .findById(genericRateDto.userId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Movie movie = movieRepository
                .findById(genericRateDto.movieId())
                .orElseThrow(() -> new EntityNotFoundException("Movie not found"));

        validateRating(genericRateDto.rating());

        S entity = rateRepository
                .findByUserIdAndMovieId(user.getId(), movie.getId())
                .map(existing -> {
                    updateEntity(existing, genericRateDto.rating());
                    return existing;
                })
                .orElseGet(() -> createEntity(user, movie, genericRateDto.rating()));

        rateRepository.save(entity);
    }

    @Transactional
    public void deleteRating(@Valid DeleteRateDto rateDto) {
        rateRepository.deleteRateByUserIdAndMovieId(rateDto.userId(), rateDto.movieId());
    }

    public void deleteRating(int userId, int movieId) {
        deleteRating(new DeleteRateDto(userId, movieId));
    }

    public List<GenericRateDto<T>> getAllByUser(int userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty())
            throw new EntityNotFoundException("User doesn't exists");

        return mapToDto(rateRepository.findByUser(user.get()));
    }

    public List<GenericRateDto<T>> getAllByMovie(int movieId) {
        Optional<Movie> movie = movieRepository.findById(movieId);
        if (movie.isEmpty())
            throw new EntityNotFoundException("no movie with this id in the database");

        return mapToDto(rateRepository.findByMovie(movie.get()));
    }

    protected abstract S createEntity(User user, Movie movie, T rating);

    protected abstract void updateEntity(S entity, T rating);

    protected abstract void validateRating(T rating);

    protected abstract List<GenericRateDto<T>> mapToDto(List<S> entities);
}
