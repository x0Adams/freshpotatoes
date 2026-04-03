package hu.pogany.freshPotato.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.springframework.validation.annotation.Validated;

@Builder
public record RateDto(
        @Min(0)
        int userId,
        @Min(0)
        int movieId,
        @Max(5)
        @Min(1)
        Byte rating
) {
}
