package hu.pogany.freshPotato.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;

@Validated
public record RateRequestDto(
        int userId,
        int movieId,
        @Max(5)
        @Min(1)
        Byte rating
) {
}
