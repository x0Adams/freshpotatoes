package hu.pogany.freshPotato.dto.rate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Builder;

@Builder
@Schema(name = "DeleteRateAdminRequest", description = "Admin payload for deleting a specific user's rating on a movie")
public record  DeleteRateDto(
        @Min(0)
        @Schema(description = "User identifier", example = "12", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
        int userId,
        @Min(0)
        @Schema(description = "Movie identifier", example = "42", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
        int movieId
) {
}
