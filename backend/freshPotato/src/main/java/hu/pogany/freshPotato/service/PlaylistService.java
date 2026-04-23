package hu.pogany.freshPotato.service;

import hu.pogany.freshPotato.dto.playlist.request.ChangePlaylistVisibilityRequestDto;
import hu.pogany.freshPotato.dto.playlist.request.CreatePlaylistRequestDto;
import hu.pogany.freshPotato.dto.playlist.request.RenamePlaylistRequestDto;
import hu.pogany.freshPotato.dto.playlist.response.PlaylistDetailsDto;
import hu.pogany.freshPotato.dto.playlist.response.PlaylistOwnerNameDto;
import hu.pogany.freshPotato.dto.response.SearchMovieDto;
import hu.pogany.freshPotato.entity.Movie;
import hu.pogany.freshPotato.entity.MovieInPlaylist;
import hu.pogany.freshPotato.entity.Playlist;
import hu.pogany.freshPotato.entity.User;
import hu.pogany.freshPotato.mapper.Mapper;
import hu.pogany.freshPotato.repository.MovieInPlaylistRepository;
import hu.pogany.freshPotato.repository.MovieRepository;
import hu.pogany.freshPotato.repository.PlaylistRepository;
import hu.pogany.freshPotato.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class PlaylistService {
    private final PlaylistRepository playlistRepository;
    private final MovieInPlaylistRepository movieInPlaylistRepository;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;
    private final Mapper mapper;

    public PlaylistService(PlaylistRepository playlistRepository,
                           MovieInPlaylistRepository movieInPlaylistRepository,
                           MovieRepository movieRepository,
                           UserRepository userRepository,
                           Mapper mapper) {
        this.playlistRepository = playlistRepository;
        this.movieInPlaylistRepository = movieInPlaylistRepository;
        this.movieRepository = movieRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Transactional
    public PlaylistOwnerNameDto createPlaylist(int requesterId, CreatePlaylistRequestDto dto) {
        User owner = userRepository.findById(requesterId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Playlist playlist = new Playlist();
        playlist.setOwner(owner);
        playlist.setName(dto.name().trim());
        playlist.setIsPrivate(dto.isPrivate());
        playlist.setCreationTime(Instant.now());

        return toOwnerNameDto(playlistRepository.save(playlist));
    }

    @Transactional
    public PlaylistOwnerNameDto renamePlaylist(int requesterId, int playlistId, RenamePlaylistRequestDto dto) {
        Playlist playlist = getPlaylistById(playlistId);
        ensureOwner(requesterId, playlist);

        playlist.setName(dto.name().trim());
        return toOwnerNameDto(playlistRepository.save(playlist));
    }

    @Transactional
    public void deletePlaylist(int requesterId, boolean isAdmin, int playlistId) {
        Playlist playlist = getPlaylistById(playlistId);
        ensureOwnerOrAdmin(requesterId, isAdmin, playlist);
        playlistRepository.delete(playlist);
    }

    @Transactional
    public PlaylistOwnerNameDto changeVisibility(int requesterId,
                                                 boolean isAdmin,
                                                 int playlistId,
                                                 ChangePlaylistVisibilityRequestDto dto) {
        Playlist playlist = getPlaylistById(playlistId);
        ensureOwnerOrAdmin(requesterId, isAdmin, playlist);

        playlist.setIsPrivate(dto.isPrivate());
        return toOwnerNameDto(playlistRepository.save(playlist));
    }

    public List<PlaylistOwnerNameDto> getPlaylistsByOwnerAndName(Integer requesterId,
                                                                  boolean isAdmin,
                                                                  int ownerId,
                                                                  String name) {
        String normalizedName = name == null ? "" : name.trim();

        return playlistRepository.findByOwnerIdAndNameContainingIgnoreCase(ownerId, normalizedName)
                .stream()
                .filter(playlist -> canReadPlaylist(requesterId, isAdmin, playlist))
                .map(this::toOwnerNameDto)
                .toList();
    }

    public PlaylistDetailsDto getPlaylistDetails(Integer requesterId, boolean isAdmin, int playlistId) {
        Playlist playlist = playlistRepository.findWithDetailsById(playlistId)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found"));

        if (!canReadPlaylist(requesterId, isAdmin, playlist)) {
            throw new EntityNotFoundException("Playlist not found");
        }

        return toDetailsDto(playlist);
    }

    @Transactional
    public PlaylistDetailsDto addMovieToPlaylist(int requesterId, int playlistId, int movieId) {
        Playlist playlist = playlistRepository.findWithDetailsById(playlistId)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found"));
        ensureOwner(requesterId, playlist);

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new EntityNotFoundException("Movie not found"));

        MovieInPlaylist relation = new MovieInPlaylist();
        relation.setPlaylist(playlist);
        relation.setMovie(movie);
        movieInPlaylistRepository.save(relation);

        Playlist updated = playlistRepository.findWithDetailsById(playlistId)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found"));
        return toDetailsDto(updated);
    }

    @Transactional
    public PlaylistDetailsDto removeMovieFromPlaylist(int requesterId, int playlistId, int movieId) {
        Playlist playlist = playlistRepository.findWithDetailsById(playlistId)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found"));
        ensureOwner(requesterId, playlist);

        MovieInPlaylist relation = movieInPlaylistRepository.findFirstByPlaylist_IdAndMovie_IdOrderByIdAsc(playlistId, movieId)
                .orElseThrow(() -> new ValidationException("Movie is not in the playlist"));

        movieInPlaylistRepository.delete(relation);

        Playlist updated = playlistRepository.findWithDetailsById(playlistId)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found"));
        return toDetailsDto(updated);
    }

    private Playlist getPlaylistById(int playlistId) {
        return playlistRepository.findById(playlistId)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found"));
    }

    private boolean canReadPlaylist(Integer requesterId, boolean isAdmin, Playlist playlist) {
        if (!Boolean.TRUE.equals(playlist.getIsPrivate())) {
            return true;
        }

        if (requesterId == null) {
            return false;
        }

        return isAdmin || Objects.equals(requesterId, playlist.getOwner().getId());
    }

    private void ensureOwner(int requesterId, Playlist playlist) {
        if (!Objects.equals(playlist.getOwner().getId(), requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can modify this playlist");
        }
    }

    private void ensureOwnerOrAdmin(int requesterId, boolean isAdmin, Playlist playlist) {
        if (isAdmin) {
            return;
        }
        ensureOwner(requesterId, playlist);
    }

    private PlaylistOwnerNameDto toOwnerNameDto(Playlist playlist) {
        return new PlaylistOwnerNameDto(
                playlist.getId(),
                playlist.getOwner().getId(),
                playlist.getOwner().getUsername(),
                playlist.getName()
        );
    }

    private PlaylistDetailsDto toDetailsDto(Playlist playlist) {
        List<SearchMovieDto> movies = playlist.getMovieInPlaylists()
                .stream()
                .sorted(Comparator.comparing(MovieInPlaylist::getId, Comparator.nullsLast(Integer::compareTo)))
                .map(MovieInPlaylist::getMovie)
                .map(mapper::toSearchMovieDto)
                .toList();

        return new PlaylistDetailsDto(
                playlist.getId(),
                playlist.getOwner().getId(),
                playlist.getOwner().getUsername(),
                playlist.getName(),
                playlist.getIsPrivate(),
                playlist.getCreationTime(),
                movies
        );
    }
}

