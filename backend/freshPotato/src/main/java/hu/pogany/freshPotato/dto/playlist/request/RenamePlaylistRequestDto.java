package hu.pogany.freshPotato.dto.playlist.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "RenamePlaylistRequest", description = "Payload for renaming a playlist")
public record RenamePlaylistRequestDto(
        @NotBlank(message = "Playlist name must not be blank")
        @Size(max = 500, message = "Playlist name must be at most 500 characters")
        @Schema(description = "New playlist name", example = "Weekend Watchlist", maxLength = 500, requiredMode = Schema.RequiredMode.REQUIRED)
        String name
) {
}

