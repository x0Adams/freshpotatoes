package hu.pogany.freshPotato.mapper;

import hu.pogany.freshPotato.dto.*;
import hu.pogany.freshPotato.dto.response.*;
import hu.pogany.freshPotato.entity.*;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapping;

import java.util.List;

@org.mapstruct.Mapper(componentModel = "spring")
public interface Mapper {
    @Mapping(target = "actors", source = "actors")
    @Mapping(target = "directors", source = "directors")
    @Mapping(target = "productionCountries", source = "countries")
    @Mapping(target = "rate", source = "averageRate")
    MovieDto toMovieDto(Movie movie);

    @Mapping(target = "birthDay", source = "birthday")
    MovieStaffDto toMovieStaffDto(Staff staff);
    GenderDto toGenderDto(Gender gender);
    CountryDto toCountryDto(Country country);
    List<MovieDto> toMovieDtoList(List<Movie> movies);
    List<SearchMovieDto> toSearchMovieDtoList(List<Movie> movies);
    SearchMovieDto toSearchMovieDto(Movie movie);
    @Mapping(target = "birthDay", source = "birthday")
    @Mapping(target = "playedMovies", ignore = true)
    @Mapping(target = "directedMovies", ignore = true)
    StaffDto toStaffDto(Staff staff);
    @Mapping(target = "birthDay", source = "birthday")
    SearchStaffDto toSearchStaffDto(Staff staff);
    List<SearchStaffDto> toSearchStaffDtoList(List<Staff> staff);
    List<GenreDto> toGenreDtoList(List<Genre> genres);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "email", source = "email")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "age", source = "age")
    @Mapping(target = "password", source = "password")
    User toUser(RegisterUserDto userDto);

    UserDtoPublic toPublicDto(UserDto userDto);
}
