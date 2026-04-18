package hu.pogany.freshPotato.dto.response;

import hu.pogany.freshPotato.dto.playlist.response.PlaylistDetailsDto;
import lombok.Builder;

import java.util.List;

@Builder
public record UserDto(
        int id,
        String username,
        String email,
        String gender,
        int age,
        List<PlaylistDetailsDto> playlists,
        List<SearchMovieDto> ratedMovies,
        List<SearchMovieDto> reviewedMovies
        ) {
}
