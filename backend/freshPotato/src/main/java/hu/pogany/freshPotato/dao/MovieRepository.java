package hu.pogany.freshPotato.dao;

import hu.pogany.freshPotato.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, String> {
    List<Movie> findAllByNameIsLike(String name);
}
