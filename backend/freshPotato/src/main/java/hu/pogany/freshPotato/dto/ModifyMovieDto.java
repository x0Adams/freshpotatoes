package hu.pogany.freshPotato.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ModifyMovieDto(
        @NotNull
        String name,
        @NotNull
        String posterPath,
        @Min(0)
        int duration,
        @NotNull
        LocalDate releaseDate,
        @NotNull
        String wikipediaTitle,
        @NotNull
        String youtubeMovie,
        @NotNull
        String trailer
) {
}
