package hu.pogany.freshPotato.repository;

import hu.pogany.freshPotato.entity.Movie;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Integer> {
    List<Movie> findTop5ByNameIsLike(String name, Sort sort);

    @Query("SELECT movie FROM Movie movie ORDER BY RAND() LIMIT 30")
    List<Movie> findRandom();
}
