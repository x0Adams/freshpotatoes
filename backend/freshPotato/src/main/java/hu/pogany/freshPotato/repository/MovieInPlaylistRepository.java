package hu.pogany.freshPotato.repository;

import hu.pogany.freshPotato.entity.MovieInPlaylist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MovieInPlaylistRepository extends JpaRepository<MovieInPlaylist, Integer> {
    Optional<MovieInPlaylist> findFirstByPlaylist_IdAndMovie_IdOrderByIdAsc(Integer playlistId, Integer movieId);
}
