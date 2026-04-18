package hu.pogany.freshPotato.service;

import hu.pogany.freshPotato.dto.playlist.response.PlaylistDetailsDto;
import hu.pogany.freshPotato.dto.response.SearchMovieDto;
import hu.pogany.freshPotato.dto.response.UserDto;
import hu.pogany.freshPotato.entity.MovieInPlaylist;
import hu.pogany.freshPotato.entity.Playlist;
import hu.pogany.freshPotato.entity.User;
import hu.pogany.freshPotato.mapper.Mapper;
import hu.pogany.freshPotato.repository.UserRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final AuthorityService authorityService;
    private final Mapper mapper;

    public UserService(UserRepository userRepository, AuthorityService authorityService, Mapper mapper) {
        this.userRepository = userRepository;
        this.authorityService = authorityService;
        this.mapper = mapper;
    }

    public int getIdByUserName(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty())
            throw new EntityNotFoundException("no user with this name");

        return user.get().getId();

    }

    public UserDto getByUserName(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("no user with this name"));

        List<PlaylistDetailsDto> playlists = user.getPlaylists().stream()
                .map(this::toPlaylistDetailsDto)
                .toList();
        List<SearchMovieDto> ratedMovies = user.getRates().stream()
                .map(it -> it.getMovie())
                .distinct()
                .map(mapper::toSearchMovieDto)
                .toList();
        List<SearchMovieDto> reviewedMovies = user.getReviews().stream()
                .map(it -> it.getMovie())
                .distinct()
                .map(mapper::toSearchMovieDto)
                .toList();

        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .gender(user.getGender().getName())
                .age(user.getAge())
                .playlists(playlists)
                .ratedMovies(ratedMovies)
                .reviewedMovies(reviewedMovies)
                .build();
    }

    private PlaylistDetailsDto toPlaylistDetailsDto(Playlist playlist) {
        List<SearchMovieDto> movies = playlist.getMovieInPlaylists().stream()
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

    public void save(User user) {
        if (isNewUserValid(user.getUsername(), user.getEmail())) {
            userRepository.save(user);
            authorityService.saveAsUser(user.getUsername());
        } else {
            throw new EntityExistsException("username or email is already used");
        }
    }

    public void saveAsAdmin(User user) {
        save(user);
        authorityService.saveAsAdmin(user.getUsername());
    }

    public boolean isNewUserValid(String username, String email) {
        return !userRepository.existsByUsername(username) && !userRepository.existsByEmail(email);
    }


}
