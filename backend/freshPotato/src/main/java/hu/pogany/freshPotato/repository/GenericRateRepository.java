package hu.pogany.freshPotato.repository;

import hu.pogany.freshPotato.entity.Movie;
import hu.pogany.freshPotato.entity.RateId;
import hu.pogany.freshPotato.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface GenericRateRepository<T> extends JpaRepository<T, RateId> {
    List<T> findByUser(User user);
    List<T> findByMovie(Movie movie);
    List<T> findByMovieId(int movieId);
    List<T> findByUserId(int userId);

    boolean existsByUserIdAndMovieId(int userId, int movieId);
    Optional<T> findByUserIdAndMovieId(int userId, int movieId);

    void deleteRateById(RateId id);

    void deleteRateByUserIdAndMovieId(Integer userId, Integer movieId);
    void deleteByMovie(Movie movie);
}
