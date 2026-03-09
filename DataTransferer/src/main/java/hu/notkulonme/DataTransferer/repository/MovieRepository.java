package hu.notkulonme.DataTransferer.repository;

import hu.notkulonme.DataTransferer.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Integer> {
}
