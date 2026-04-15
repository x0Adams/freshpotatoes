package hu.pogany.freshPotato.dto.playlist.response;

import hu.pogany.freshPotato.dto.response.SearchMovieDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(name = "PlaylistDetails", description = "Complete playlist response")
public record PlaylistDetailsDto(
        @Schema(description = "Playlist id", example = "15")
        Integer id,
        @Schema(description = "Owner user id", example = "7")
        Integer ownerId,
        @Schema(description = "Owner username", example = "john_doe")
        String ownerName,
        @Schema(description = "Playlist name", example = "Top 10 Thrillers")
        String name,
        @Schema(description = "Whether the playlist is private", example = "true")
        Boolean isPrivate,
        @Schema(description = "Creation timestamp in UTC", example = "2026-04-15T12:30:00Z")
        Instant creationTime,
        @Schema(description = "Movies in the playlist")
        List<SearchMovieDto> movies
) {
}

