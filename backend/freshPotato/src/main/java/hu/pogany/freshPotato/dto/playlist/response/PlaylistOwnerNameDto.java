package hu.pogany.freshPotato.dto.playlist.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PlaylistOwnerName", description = "Minimal playlist projection containing owner and playlist name")
public record PlaylistOwnerNameDto(
        @Schema(description = "Playlist id", example = "15")
        Integer id,
        @Schema(description = "Owner user id", example = "7")
        Integer ownerId,
        @Schema(description = "Owner username", example = "john_doe")
        String ownerName,
        @Schema(description = "Playlist name", example = "Top 10 Thrillers")
        String name
) {
}

