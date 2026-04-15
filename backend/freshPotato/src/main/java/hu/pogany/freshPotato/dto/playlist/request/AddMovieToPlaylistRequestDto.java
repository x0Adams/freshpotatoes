package hu.pogany.freshPotato.dto.playlist.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(name = "AddMovieToPlaylistRequest", description = "Payload for adding a movie to a playlist")
public record AddMovieToPlaylistRequestDto(
        @NotNull(message = "movieId is required")
        @Min(value = 1, message = "movieId must be greater than 0")
        @Schema(description = "Movie identifier", example = "42", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer movieId
) {
}

