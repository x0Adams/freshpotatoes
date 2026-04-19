package hu.pogany.freshPotato.dto.playlist.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "ChangePlaylistVisibilityRequest", description = "Payload for changing playlist visibility")
public record ChangePlaylistVisibilityRequestDto(
        @NotNull(message = "isPrivate is required")
        @Schema(description = "Whether the playlist should be private", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean isPrivate
) {
}

