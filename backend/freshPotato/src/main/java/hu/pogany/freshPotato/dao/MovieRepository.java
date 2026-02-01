package hu.pogany.freshPotato.dao;

import hu.pogany.freshPotato.entity.Movie;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, String> {
    List<Movie> findTop5ByNameIsLike(String name, Sort sort);
    @Query("SELECT movie FROM Movie movie ORDER BY RAND() LIMIT 30")
    List<Movie> findRandom();
}
