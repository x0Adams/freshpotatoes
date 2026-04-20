package hu.pogany.freshPotato.repository;

import hu.pogany.freshPotato.entity.Movie;
import hu.pogany.freshPotato.entity.StaffRole;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Integer>, JpaSpecificationExecutor<Movie> {
    List<Movie> findTop5ByNameIsLike(String name, Sort sort);

    @Query("SELECT movie FROM Movie movie where movie.name != 'None' and movie.wikipediaTitle != 'None' ORDER BY RAND() LIMIT 30")
    List<Movie> findRandom();

    @Query("select m from Movie m left join m.views v group by m order by count(v) desc")
    List<Movie> findByPopularity(Pageable pageable);

    @Query("select distinct m from Movie m join m.staffRoleInMovies srm where srm.staff.id = :staffId and srm.id.role = :role")
    List<Movie> findByStaffAndRole(@Param("staffId") Integer staffId, @Param("role") StaffRole role);

    @EntityGraph(attributePaths = {"genres"})
    List<Movie> findByIdIn(List<Integer> ids);
}
