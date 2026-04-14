package hu.pogany.freshPotato.dto.response;

import java.time.LocalDate;
import java.util.Set;

public record MovieDto(String id,
                       String name,
                       String posterPath,
                       Integer duration,
                       LocalDate releaseDate,
                       String youtubeMovie,
                       String wikipediaTitle,
                       String trailer,
                       Double rate,
                       Set<GenreDto> genres,
                       Set<MovieStaffDto> actors,
                       Set<MovieStaffDto> directors,
                       Set<CountryDto> productionCountries
                       ){
}
