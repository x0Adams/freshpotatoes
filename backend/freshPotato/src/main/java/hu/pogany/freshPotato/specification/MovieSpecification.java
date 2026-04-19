package hu.pogany.freshPotato.specification;

import hu.pogany.freshPotato.entity.Movie;
import hu.pogany.freshPotato.entity.Staff;
import hu.pogany.freshPotato.entity.StaffRoleInMovie;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MovieSpecification {
    private MovieSpecification() {
    }

    public static Specification<Movie> titleExactOrPrefix(String title) {
        return (root, query, cb) -> {
            String normalizedTitle = normalize(title);
            if (normalizedTitle == null) {
                return cb.conjunction();
            }

            Expression<String> movieName = cb.lower(root.get("name"));
            return cb.or(
                    cb.equal(movieName, normalizedTitle),
                    cb.like(movieName, normalizedTitle + "%")
            );
        };
    }

    public static Specification<Movie> hasStaff(List<String> staffNames) {
        return (root, query, cb) -> {
            List<String> normalizedStaffNames = normalizeList(staffNames);
            if (normalizedStaffNames.isEmpty()) {
                return cb.conjunction();
            }

            query.distinct(true);
            Join<Movie, StaffRoleInMovie> staffRoleInMovieJoin = root.join("staffRoleInMovies", JoinType.INNER);
            Join<StaffRoleInMovie, Staff> staffJoin = staffRoleInMovieJoin.join("staff", JoinType.INNER);
            return cb.lower(staffJoin.get("name")).in(normalizedStaffNames);
        };
    }

    public static Specification<Movie> hasGenres(List<String> genreNames) {
        return (root, query, cb) -> {
            List<String> normalizedGenres = normalizeList(genreNames);
            if (normalizedGenres.isEmpty()) {
                return cb.conjunction();
            }

            query.distinct(true);
            Join<Object, Object> genreJoin = root.join("genres", JoinType.INNER);
            return cb.lower(genreJoin.get("name")).in(normalizedGenres);
        };
    }

    public static Specification<Movie> orderByExactMatchThenPopularity(String title) {
        return (root, query, cb) -> {
            String normalizedTitle = normalize(title);
            if (normalizedTitle == null || Long.class.equals(query.getResultType())) {
                return cb.conjunction();
            }

            Expression<Integer> relevanceOrder = cb.<Integer>selectCase()
                    .when(cb.equal(cb.lower(root.get("name")), normalizedTitle), 0)
                    .otherwise(1);

            query.orderBy(
                    cb.asc(relevanceOrder),
                    cb.desc(cb.size(root.get("views"))),
                    cb.asc(cb.lower(root.get("name")))
            );

            return cb.conjunction();
        };
    }

    private static List<String> normalizeList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }

        List<String> normalizedValues = new ArrayList<>();
        for (String value : values) {
            String normalized = normalize(value);
            if (normalized != null) {
                normalizedValues.add(normalized);
            }
        }
        return normalizedValues;
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        return trimmed.toLowerCase(Locale.ROOT);
    }
}
