package hu.pogany.freshPotato.dto.response;

import java.time.LocalDate;
import java.util.Set;

public record SearchMovieDto(
        String id,
        String name,
        String posterPath,
        Set<GenreDto> genres,
        LocalDate releaseDate
) {
}
