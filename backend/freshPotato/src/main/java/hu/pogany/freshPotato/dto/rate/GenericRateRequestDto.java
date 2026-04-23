package hu.pogany.freshPotato.dto.rate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Base request payload for movie rating operations")
public class GenericRateRequestDto<T> {

    @NotNull(message = "rating is required")
    @Schema(description = "Rating value. Concrete subclasses define allowed range.", requiredMode = Schema.RequiredMode.REQUIRED)
    private T rate;
    @Min(0)
    @Schema(description = "Movie identifier", example = "42", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private int movieId;

    public GenericRateRequestDto() {
    }

    public GenericRateRequestDto(T rate, int movieId) {
        this.rate = rate;
        this.movieId = movieId;
    }

    public void setRate(T rate) {
        this.rate = rate;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public T rate() {
        return rate;
    }

    public T getRate() {
        return rate;
    }

    public int movieId() {
        return movieId;
    }

    public int getMovieId() {
        return movieId;
    }
}
