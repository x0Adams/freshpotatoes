package hu.pogany.freshPotato.dto.playlist.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "CreatePlaylistRequest", description = "Payload for creating a playlist")
public record CreatePlaylistRequestDto(
        @NotBlank(message = "Playlist name must not be blank")
        @Size(max = 500, message = "Playlist name must be at most 500 characters")
        @Schema(description = "Playlist name", example = "My Sci-Fi Favorites", maxLength = 500, requiredMode = Schema.RequiredMode.REQUIRED)
        String name,

        @NotNull(message = "isPrivate is required")
        @Schema(description = "Whether the playlist is private", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean isPrivate
) {
}

