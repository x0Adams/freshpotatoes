package hu.pogany.freshPotato.dto;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;


public record RateRequestDto(
        @Min(1)
        @Max(5)
        byte rate,
        @Min(0)
        int movieId
) {
}
