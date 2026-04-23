package hu.pogany.freshPotato.dto.rate;

import hu.pogany.freshPotato.dto.response.GenreDto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenericRateDto<T> {
    @Min(value = 0, message = "userid must be greater than zero")
    private int userId;
    @Min(value = 0, message = "movie id must be greater than zero")
    private int movieId;
    private String username;
    private String name;
    private String posterPath;
    private Set<GenreDto> genres;
    private LocalDate releaseDate;

    @NotNull
    private T rating;

}
