package hu.pogany.freshPotato.dto.rate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenericRateDto<T> {
    @Min(value = 0, message = "userid must be greater than zero")
    private final int userId;
    @Min(value = 0, message = "movie id must be greater than zero")
    private final int movieId;
    @NotNull
    private final T rating;

    public GenericRateDto(int userId, int movieId, T rating) {
        this.userId = userId;
        this.movieId = movieId;
        this.rating = rating;
    }

    public int userId() {
        return userId;
    }

    public int movieId() {
        return movieId;
    }

    public T rating() {
        return rating;
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static final class Builder<T> {
        private int userId;
        private int movieId;
        private T rating;

        public Builder<T> userId(int userId) {
            this.userId = userId;
            return this;
        }

        public Builder<T> movieId(int movieId) {
            this.movieId = movieId;
            return this;
        }

        public Builder<T> rating(T rating) {
            this.rating = rating;
            return this;
        }

        public GenericRateDto<T> build() {
            return new GenericRateDto<>(userId, movieId, rating);
        }
    }
}
