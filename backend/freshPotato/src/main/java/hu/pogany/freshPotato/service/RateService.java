package hu.pogany.freshPotato.service;

import hu.pogany.freshPotato.dto.RateDto;
import hu.pogany.freshPotato.entity.Movie;
import hu.pogany.freshPotato.entity.Rate;
import hu.pogany.freshPotato.entity.User;
import hu.pogany.freshPotato.repository.MovieRepository;
import hu.pogany.freshPotato.repository.RateRepository;
import hu.pogany.freshPotato.repository.UserRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
public class RateService {
    private final RateRepository rateRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;

    public RateService(RateRepository rateRepository, UserRepository userRepository, MovieRepository movieRepository) {
        this.rateRepository = rateRepository;
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
    }

    @Transactional(readOnly = false)
    public void saveRating(RateDto rateDto) {
        User user = userRepository
                .findById(rateDto.userId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Movie movie = movieRepository
                .findById(rateDto.movieId())
                .orElseThrow(() -> new EntityNotFoundException("Movie not found"));

        Rate rate = new Rate();
        rate.setMovie(movie);
        rate.setUser(user);

        if (rateRepository.existsByUserIdAndMovieId(user.getId(), movie.getId()))
            throw new EntityExistsException("rating is already exists");

        rateRepository.save(rate);
    }

    @Transactional(readOnly = false)
    public void deleteRating(){}

}
