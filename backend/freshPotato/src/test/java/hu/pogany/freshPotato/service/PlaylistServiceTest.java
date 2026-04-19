package hu.pogany.freshPotato.service;

import hu.pogany.freshPotato.dto.playlist.request.ChangePlaylistVisibilityRequestDto;
import hu.pogany.freshPotato.dto.playlist.request.CreatePlaylistRequestDto;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaylistServiceTest {

    @Mock
    private PlaylistRepository playlistRepository;

    @Mock
    private MovieInPlaylistRepository movieInPlaylistRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Mapper mapper;

    @InjectMocks
    private PlaylistService playlistService;

    @Test
    void createPlaylist_shouldThrowEntityNotFoundException_whenAuthenticatedOwnerDoesNotExistInDatabase() {
        when(userRepository.findById(42)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> playlistService.createPlaylist(42, new CreatePlaylistRequestDto("Name", true))
        );

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void deletePlaylist_shouldThrowForbiddenResponseStatusException_whenRequesterIsNeitherOwnerNorAdmin() {
        Playlist playlist = createPlaylist(5, true);
        when(playlistRepository.findById(9)).thenReturn(Optional.of(playlist));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> playlistService.deletePlaylist(7, false, 9)
        );

        assertEquals(403, exception.getStatusCode().value());
        verify(playlistRepository, never()).delete(any());
    }

    @Test
    void deletePlaylist_shouldDeletePlaylist_whenRequesterIsAdminEvenIfNotOwner() {
        Playlist playlist = createPlaylist(5, true);
        when(playlistRepository.findById(9)).thenReturn(Optional.of(playlist));

        playlistService.deletePlaylist(7, true, 9);

        verify(playlistRepository).delete(playlist);
    }

    @Test
    void changeVisibility_shouldPersistUpdatedVisibility_whenRequesterIsAdminForForeignPlaylist() {
        Playlist playlist = createPlaylist(5, true);
        playlist.setId(3);
        when(playlistRepository.findById(3)).thenReturn(Optional.of(playlist));
        when(playlistRepository.save(any(Playlist.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlaylistOwnerNameDto result = playlistService.changeVisibility(
                99,
                true,
                3,
                new ChangePlaylistVisibilityRequestDto(false)
        );

        assertFalse(playlist.getIsPrivate());
        assertEquals(3, result.id());
        verify(playlistRepository).save(playlist);
    }

    @Test
    void addMovieToPlaylist_shouldCreateNewRelation_whenSameMovieIsAlreadyPresentInPlaylistBecauseDuplicatesAreAllowed() {
        Playlist playlist = createPlaylist(5, false);
        Movie movie = new Movie();
        movie.setId(100);

        when(playlistRepository.findWithDetailsById(9)).thenReturn(Optional.of(playlist));
        when(movieRepository.findById(100)).thenReturn(Optional.of(movie));

        Playlist updatedPlaylist = createPlaylist(5, false);
        updatedPlaylist.setId(9);
        when(playlistRepository.findWithDetailsById(9)).thenReturn(Optional.of(playlist)).thenReturn(Optional.of(updatedPlaylist));

        playlistService.addMovieToPlaylist(5, 9, 100);

        verify(movieInPlaylistRepository).save(any(MovieInPlaylist.class));
        verify(movieRepository).findById(100);
    }

    @Test
    void removeMovieFromPlaylist_shouldThrowValidationException_whenMovieIsNotInPlaylist() {
        Playlist playlist = createPlaylist(5, false);
        when(playlistRepository.findWithDetailsById(9)).thenReturn(Optional.of(playlist));
        when(movieInPlaylistRepository.findFirstByPlaylist_IdAndMovie_IdOrderByIdAsc(9, 100)).thenReturn(Optional.empty());

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> playlistService.removeMovieFromPlaylist(5, 9, 100)
        );

        assertEquals("Movie is not in the playlist", exception.getMessage());
        verify(movieInPlaylistRepository, never()).delete(any(MovieInPlaylist.class));
    }

    @Test
    void removeMovieFromPlaylist_shouldDeleteTheOldestMatchingRelation_whenMovieExistsMultipleTimesInPlaylist() {
        Playlist playlist = createPlaylist(5, false);
        Movie movie = new Movie();
        movie.setId(100);

        MovieInPlaylist relation = new MovieInPlaylist();
        relation.setId(11);
        relation.setMovie(movie);
        relation.setPlaylist(playlist);

        Playlist updatedPlaylist = createPlaylist(5, false);
        updatedPlaylist.setId(9);

        when(playlistRepository.findWithDetailsById(9)).thenReturn(Optional.of(playlist));
        when(movieInPlaylistRepository.findFirstByPlaylist_IdAndMovie_IdOrderByIdAsc(9, 100)).thenReturn(Optional.of(relation));
        when(playlistRepository.findWithDetailsById(9)).thenReturn(Optional.of(playlist)).thenReturn(Optional.of(updatedPlaylist));

        playlistService.removeMovieFromPlaylist(5, 9, 100);

        verify(movieInPlaylistRepository).delete(eq(relation));
    }

    @Test
    void getPlaylistDetails_shouldThrowEntityNotFoundException_whenPlaylistIsPrivateAndRequesterIsNotOwnerOrAdmin() {
        Playlist playlist = createPlaylist(5, true);
        when(playlistRepository.findWithDetailsById(9)).thenReturn(Optional.of(playlist));

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> playlistService.getPlaylistDetails(8, false, 9)
        );

        assertEquals("Playlist not found", exception.getMessage());
    }

    @Test
    void getPlaylistsByOwnerAndName_shouldReturnOnlyPublicPlaylists_whenRequesterIsAnonymous() {
        Playlist publicPlaylist = createPlaylist(5, false);
        publicPlaylist.setId(1);
        publicPlaylist.setName("Public list");

        Playlist privatePlaylist = createPlaylist(5, true);
        privatePlaylist.setId(2);
        privatePlaylist.setName("Private list");

        when(playlistRepository.findByOwnerIdAndNameContainingIgnoreCase(5, "")).thenReturn(List.of(publicPlaylist, privatePlaylist));

        List<PlaylistOwnerNameDto> result = playlistService.getPlaylistsByOwnerAndName(null, false, 5, "");

        assertEquals(1, result.size());
        assertEquals(1, result.getFirst().id());
        assertEquals("Public list", result.getFirst().name());
    }

    @Test
    void getPlaylistDetails_shouldReturnMoviesSortedByMovieInPlaylistIdAndKeepDuplicates_whenRequesterIsOwnerOfPrivatePlaylist() {
        Playlist playlist = createPlaylist(5, true);
        playlist.setId(9);

        Movie firstMovie = new Movie();
        firstMovie.setId(100);
        firstMovie.setName("Movie 100");

        Movie secondMovie = new Movie();
        secondMovie.setId(200);
        secondMovie.setName("Movie 200");

        MovieInPlaylist laterRelation = new MovieInPlaylist();
        laterRelation.setId(20);
        laterRelation.setMovie(secondMovie);
        laterRelation.setPlaylist(playlist);

        MovieInPlaylist earlierRelation = new MovieInPlaylist();
        earlierRelation.setId(10);
        earlierRelation.setMovie(firstMovie);
        earlierRelation.setPlaylist(playlist);

        MovieInPlaylist duplicateRelation = new MovieInPlaylist();
        duplicateRelation.setId(30);
        duplicateRelation.setMovie(firstMovie);
        duplicateRelation.setPlaylist(playlist);

        playlist.setMovieInPlaylists(new java.util.LinkedHashSet<>(List.of(laterRelation, earlierRelation, duplicateRelation)));

        when(playlistRepository.findWithDetailsById(9)).thenReturn(Optional.of(playlist));
        when(mapper.toSearchMovieDto(firstMovie)).thenReturn(new SearchMovieDto("100", "Movie 100", null, null, null));
        when(mapper.toSearchMovieDto(secondMovie)).thenReturn(new SearchMovieDto("200", "Movie 200", null, null, null));

        var result = playlistService.getPlaylistDetails(5, false, 9);

        assertEquals(3, result.movies().size());
        assertEquals("100", result.movies().getFirst().id());
        assertEquals("200", result.movies().get(1).id());
        assertEquals("100", result.movies().get(2).id());
    }

    private Playlist createPlaylist(int ownerId, boolean isPrivate) {
        User owner = new User();
        owner.setId(ownerId);
        owner.setUsername("owner" + ownerId);

        Playlist playlist = new Playlist();
        playlist.setOwner(owner);
        playlist.setName("Playlist");
        playlist.setIsPrivate(isPrivate);
        return playlist;
    }
}

