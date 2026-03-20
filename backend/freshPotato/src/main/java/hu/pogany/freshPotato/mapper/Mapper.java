package hu.pogany.freshPotato.mapper;

import hu.pogany.freshPotato.dto.*;
import hu.pogany.freshPotato.entity.Country;
import hu.pogany.freshPotato.entity.Gender;
import hu.pogany.freshPotato.entity.Movie;
import hu.pogany.freshPotato.entity.Staff;
import org.mapstruct.Mapping;

import java.util.List;

@org.mapstruct.Mapper(componentModel = "spring")
public interface Mapper {
    @Mapping(target = "actors", source = "actors")
    @Mapping(target = "directors", source = "directors")
    @Mapping(target = "productionCountries", source = "countries")
    MovieDto toMovieDto(Movie movie);
    Movie toMovie(MovieDto movieDto);
    @Mapping(target = "birthDay", source = "birthday")
    MovieStaffDto toMovieStaffDto(Staff staff);
    GenderDto toGenderDto(Gender gender);
    CountryDto toCountryDto(Country country);
    List<MovieDto> toMovieDtoList(List<Movie> movies);
    List<SearchMovieDto> toSearchMovieDtoList(List<Movie> movies);
    SearchMovieDto toSearchMovieDto(Movie movie);
}
