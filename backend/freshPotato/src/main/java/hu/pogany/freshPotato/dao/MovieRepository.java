package hu.pogany.freshPotato.dao;

import hu.pogany.freshPotato.entity.Movie;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, String> {
    List<Movie> findTop5ByNameIsLike(String name, Sort sort);
}
