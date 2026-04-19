package hu.pogany.freshPotato.dto.rate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

@Schema(name = "DeleteRateRequest", description = "Request payload for deleting the authenticated user's rating for a movie")
public record DeleteRateRequestDto(
    @Min(0)
    @Schema(description = "Movie identifier", example = "42", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    int movieId
){}
