package hu.pogany.freshPotato.repository;

import hu.pogany.freshPotato.entity.Movie;
import hu.pogany.freshPotato.entity.StaffRoleInMovie;
import hu.pogany.freshPotato.entity.StaffRoleInMovieId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffRoleInMovieRepository extends JpaRepository<StaffRoleInMovie, StaffRoleInMovieId> {
    void deleteByMovie(Movie movie);
}

