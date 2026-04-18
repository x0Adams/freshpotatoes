package hu.pogany.freshPotato.dto.response;

import java.time.LocalDate;
import java.util.List;

public record StaffDto(
        Integer id,
        String name,
        GenderDto gender,
        LocalDate birthDay,
        CountryDto birthCountry,
        List<SearchMovieDto> playedMovies,
        List<SearchMovieDto> directedMovies
) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer id;
        private String name;
        private GenderDto gender;
        private LocalDate birthDay;
        private CountryDto birthCountry;
        private List<SearchMovieDto> playedMovies;
        private List<SearchMovieDto> directedMovies;

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder gender(GenderDto gender) {
            this.gender = gender;
            return this;
        }

        public Builder birthDay(LocalDate birthDay) {
            this.birthDay = birthDay;
            return this;
        }

        public Builder birthCountry(CountryDto birthCountry) {
            this.birthCountry = birthCountry;
            return this;
        }

        public Builder playedMovies(List<SearchMovieDto> playedMovies) {
            this.playedMovies = playedMovies;
            return this;
        }

        public Builder directedMovies(List<SearchMovieDto> directedMovies) {
            this.directedMovies = directedMovies;
            return this;
        }

        public StaffDto build() {
            return new StaffDto(id, name, gender, birthDay, birthCountry, playedMovies, directedMovies);
        }
    }
}

