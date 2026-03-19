package hu.notkulonme.DataTransferer.entity.dto;

import hu.notkulonme.DataTransferer.entity.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Document(collection = "movie")
public record FilmMongo(
        @Id
        String qid,
        String title,
        int duration,
        String releaseDate,
        List<String> genres,
        List<String> actors,
        List<String> directors,
        List<String> productionCountries,
        String youtubeId,
        String wikipediaTitle
) implements DumpDocument{
    public Movie toEntity(Map<Integer, Genre> genres, Map<Integer, Staff> staffs, Map<Integer, Country> countries) {
        Movie movie = new Movie();
        movie.setId(getIdFromQid());
        movie.setName(resolveTitle());
        movie.setDuration(duration);
        movie.setReleaseDate(parseNullableDate(releaseDate));
        movie.setYoutubeMovie(youtubeId);
        movie.setWikipediaTitle(wikipediaTitle);

        movie.setGenres(
                safeList(this.genres).stream()
                        .map(this::parseQid)
                        .filter(id -> id > 0)
                        .distinct()
                        .map(genres::get)
                        .filter(Objects::nonNull)
                        .limit(3)
                        .collect(Collectors.toSet())
        );

        Set<StaffRoleInMovie> topDirectors = getStaffRoleSet(staffs, movie, directors, "director", 2);
        Set<StaffRoleInMovie> topActors = getStaffRoleSet(staffs, movie, actors, "actor", 5);
        topDirectors.addAll(topActors);
        movie.setStaffRoleInMovies(topDirectors);

        movie.setCountries(
                safeList(productionCountries).stream()
                        .map(this::parseQid)
                        .filter(id -> id > 0)
                        .distinct()
                        .map(countries::get)
                        .filter(Objects::nonNull)
                        .limit(3)
                        .collect(Collectors.toCollection(HashSet::new))
        );

        movie.setTrailer("Not Fetched");
        movie.setPosterPath("Not Fetched");
        return movie;
    }

    private String resolveTitle() {
        if (title != null && !title.isBlank()) {
            return title;
        }
        String wikiTitle = safeName(wikipediaTitle);
        String normalized = wikiTitle.replaceFirst("(?i)\\s*\\(movie\\)$", "").trim();
        return normalized.isBlank() ? "None" : normalized;
    }

    private LocalDate parseNullableDate(String date) {
        if (date == null || date.isBlank()) {
            return null;
        }
        List<String> parts = splitDate(date);
        if (parts.size() < 3) {
            return null;
        }
        int year = toIntOrDefault(parts.get(0), 1900);
        int month = toIntOrDefault(parts.get(1), 1);
        int day = toIntOrDefault(parts.get(2), 1);
        month = month <= 0 ? 1 : month;
        day = day <= 0 ? 1 : day;
        try {
            return LocalDate.of(year, month, day);
        } catch (DateTimeException e) {
            return null;
        }
    }

    private Set<StaffRoleInMovie> getStaffRoleSet(Map<Integer, Staff> staffs, Movie movie, List<String> qids, String role, int limit) {
        return safeList(qids).stream()
                .map(this::parseQid)
                .filter(id -> id > 0)
                .distinct()
                .filter(staffs::containsKey)
                .limit(limit)
                .map(id -> {
                    Staff staff = staffs.get(id);
                    StaffRoleInMovieId objId = new StaffRoleInMovieId();
                    objId.setMovie(movie.getId());
                    objId.setStaff(staff.getId());
                    objId.setRole(role);

                    StaffRoleInMovie connector = new StaffRoleInMovie();
                    connector.setId(objId);
                    connector.setStaff(staff);
                    connector.setMovie(movie);
                    return connector;
                })
                .collect(Collectors.toCollection(HashSet::new));
    }
}
