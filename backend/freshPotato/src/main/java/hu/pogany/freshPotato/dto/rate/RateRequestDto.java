package hu.pogany.freshPotato.dto.rate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(name = "RateMovieRequest", description = "Request payload for creating or updating a movie rating")
public class RateRequestDto extends GenericRateRequestDto<Integer>{

    @Min(value = 1, message = "rating must be: 1 ≤ rating ≤ 5")
    @Max(value = 5, message = "rating must be: 1 ≤ rating ≤ 5")
    @Schema(description = "Rating score from 1 to 5", example = "4", minimum = "1", maximum = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    @Override
    public Integer rate() {
        return super.rate();
    }
}
