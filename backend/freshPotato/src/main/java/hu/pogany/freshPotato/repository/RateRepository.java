package hu.pogany.freshPotato.repository;

import hu.pogany.freshPotato.entity.Movie;
import hu.pogany.freshPotato.entity.Rate;
import hu.pogany.freshPotato.entity.RateId;
import hu.pogany.freshPotato.entity.User;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RateRepository extends JpaRepository<Rate, RateId> {
    List<Rate> findByUser(User user);
    List<Rate> findByMovie(Movie movie);
    boolean existsByUserIdAndMovieId(int userId, int movieId);
    Optional<Rate> findByUserIdAndMovieId(int userId, int movieId);
}
