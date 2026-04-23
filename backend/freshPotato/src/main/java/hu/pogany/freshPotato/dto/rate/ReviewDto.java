package hu.pogany.freshPotato.dto.rate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "ReviewDto", description = "Movie review entry")
public record ReviewDto(
        @Min(0)
        @Schema(description = "User identifier", example = "12", minimum = "0")
        int userId,
        @Min(0)
        @Schema(description = "Movie identifier", example = "42", minimum = "0")
        int movieId,
        @NotBlank
        @Size(max = 4000)
        @Schema(description = "Review text", example = "Great pacing, visuals and soundtrack.", maxLength = 4000)
        String rating
) {}
