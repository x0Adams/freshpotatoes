package hu.pogany.freshPotato.dto.rate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "ReviewMovieRequest", description = "Request payload for creating or updating a movie review")
public class ReviewRequestDto extends GenericRateRequestDto<String> {

    @Override
    @NotBlank(message = "Review text must not be blank")
    @Size(max = 4000, message = "Review text must be at most 4000 characters")
    @Schema(description = "Review text", example = "Great pacing, visuals and soundtrack.", maxLength = 4000, requiredMode = Schema.RequiredMode.REQUIRED)
    public String getRate() {
        return super.rate();
    }
}
