package hu.pogany.freshPotato.dto.rate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(name = "RateDto", description = "Movie rating entry")
public record RateDto(
        @Min(0)
        @Schema(description = "User identifier", example = "12", minimum = "0")
        int userId,
        @Min(0)
        @Schema(description = "Movie identifier", example = "42", minimum = "0")
        int movieId,
        @NotNull
        @Min(1)
        @Max(5)
        @Schema(description = "Rating score from 1 to 5", example = "4", minimum = "1", maximum = "5")
        Integer rating
) {}
