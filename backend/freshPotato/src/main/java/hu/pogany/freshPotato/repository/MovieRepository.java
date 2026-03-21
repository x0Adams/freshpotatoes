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

    @Query("SELECT movie FROM Movie movie where movie.name != 'None' and movie.wikipediaTitle != 'None' ORDER BY RAND() LIMIT 30")
    List<Movie> findRandom();

    @Query("select m from Movie m left join m.views v group by m order by count(v) desc ")
    List<Movie> findByPopularity();
}
