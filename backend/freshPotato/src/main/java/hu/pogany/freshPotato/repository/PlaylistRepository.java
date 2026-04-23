package hu.pogany.freshPotato.repository;

import hu.pogany.freshPotato.entity.Playlist;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlaylistRepository extends JpaRepository<Playlist, Integer> {
    List<Playlist> findByOwnerIdAndNameContainingIgnoreCase(Integer ownerId, String name);

    @EntityGraph(attributePaths = {
            "owner",
            "movieInPlaylists",
            "movieInPlaylists.movie",
            "movieInPlaylists.movie.genres"
    })
    Optional<Playlist> findWithDetailsById(Integer id);
}

