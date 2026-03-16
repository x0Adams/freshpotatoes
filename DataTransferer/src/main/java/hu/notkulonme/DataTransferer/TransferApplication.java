package hu.notkulonme.DataTransferer;


import hu.notkulonme.DataTransferer.entity.*;
import hu.notkulonme.DataTransferer.entity.dto.*;
import hu.notkulonme.DataTransferer.repository.MovieRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransferApplication {
    MongoTemplate mongo;
    MovieRepository movies;

    public TransferApplication(MongoTemplate mongo, MovieRepository movies) {
        this.mongo = mongo;
        this.movies = movies;
    }

    public void transfer() {
        Map<Integer, Continent> continents = getContinentMap(mongo.findAll(ContinentMongo.class));

        Map<Integer, Country> countries = getCountryMap(mongo.findAll(CountryMongo.class), continents);


        Map<Integer, Gender> genders = getGenderMap(mongo.findAll(GenderMongo.class));

        Map<Integer, Genre> genres = getGenreMap( mongo.findAll(GenreMongo.class));

        Map<Integer, Staff> staffs = mongo.findAll(StaffMongo.class).stream()
                .map(it -> {
                    Staff staff = new Staff();
                    staff.setId(it.getIdFromQid());
                    staff.setName(it.name());
                    int countryCode = Integer.parseInt(it.citizenships().getFirst().substring(1));
                    staff.setBirthCountry(countries.get(countryCode));
                    var buffer = it.birthday().split("-");
                    int year = Integer.parseInt(buffer[0]);
                    int month = Integer.parseInt(buffer[1]) == 0 ? 1 : Integer.parseInt(buffer[1]);
                    int day = Integer.parseInt(buffer[2]) == 0 ? 1 : Integer.parseInt(buffer[2]);

                    staff.setBirthday(LocalDate.of(year, month, day));
                    return staff;
                })
                .collect(
                        HashMap::new,
                        (map, element) -> map.put(element.getId(), element),
                        HashMap::putAll
                );

        List<Movie> movieList = mongo.findAll(FilmMongo.class).stream()
                .map(it -> {
                    Movie movie = new Movie();
                    movie.setId(it.getIdFromQid());
                    movie.setName(it.title());
                    movie.setDuration(it.duration());
                    var buffer = it.releaseDate().split("-");
                    int year = Integer.parseInt(buffer[0]);
                    int month = Integer.parseInt(buffer[1]) == 0 ? 1 : Integer.parseInt(buffer[1]);
                    int day = Integer.parseInt(buffer[2]) == 0 ? 1 : Integer.parseInt(buffer[2]);
                    movie.setReleaseDate(LocalDate.of(year, month, day));
                    movie.setYoutubeMovie(it.youtubeId());
                    var topGenres = it.genres().stream()
                            .mapToInt(qid -> Integer.parseInt(qid.substring(1)))
                            .filter(genres::containsKey)
                            .limit(3)
                            .mapToObj(genres::get)
                            .collect(Collectors.toSet());
                    movie.setGenres(topGenres);

                    var topDirectors = getStaffRoleSet(staffs, movie, it.directors(), "director", 2);
                    var topActors = getStaffRoleSet(staffs, movie, it.directors(), "actor", 5);

                    topDirectors.addAll(topActors);

                    movie.setStaffRoleInMovies(topDirectors);

                    movie.setCountries(
                            it.productionCountries().stream()
                                    .mapToInt(qid -> Integer.parseInt(qid.substring(1)))
                                    .filter(countries::containsKey)
                                    .limit(3)
                                    .mapToObj(countries::get)
                                    .collect(Collectors.toSet())
                    );

                    movie.setTrailer("Not Fetched");
                    movie.setPosterPath("Not Fetched");

                    return movie;

                })
                .toList();

        System.out.println(movieList.size());


        saveMovies(movieList);

    }

    @Transactional
    public void saveMovies(List<Movie> movieList) {
        movies.saveAll(movieList);
    }

    private Map<Integer, Genre> getGenreMap(List<GenreMongo> genres) {
        return genres.stream()
                .map(it -> {
                    Genre genre = new Genre();
                    genre.setId(it.getIdFromQid());
                    genre.setName(it.name());
                    return genre;
                })
                .collect(
                        HashMap::new,
                        (map, element) -> map.put(element.getId(), element),
                        HashMap::putAll
                );
    }

    private Map<Integer, Gender> getGenderMap(List<GenderMongo> genders) {
        return genders.stream().map(it -> {
                    Gender gender = new Gender();
                    gender.setId(it.getIdFromQid());
                    gender.setName(it.name());
                    return gender;
                })
                .collect(
                        HashMap::new,
                        (map, element) -> map.put(element.getId(), element),
                        HashMap::putAll
                );
    }

    private Map<Integer, Country> getCountryMap(List<CountryMongo> countries, Map<Integer, Continent> continents) {
        return countries.stream()
                .map(it -> {
                    Country country = new Country();
                    country.setId(it.getIdFromQid());
                    country.setName(it.name());
                    HashSet<Continent> continentList = it.continentQid().stream()
                            .mapToInt(con -> {
                                try {
                                    return Integer.parseInt(con.substring(1)) ;
                                } catch (NumberFormatException e) {
                                    return 0;
                                }

                            }
                            )
                            .mapToObj(continents::get)
                            .collect(Collectors.toCollection(HashSet::new));
                    country.setContinents(continentList);
                    return country;
                })
                .collect(
                        HashMap::new,
                        (map, element) -> map.put(element.getId(), element),
                        HashMap::putAll
                );
    }

    private Map<Integer, Continent> getContinentMap(List<ContinentMongo> continents) {
        return continents.stream()
                .map(it -> {
                    Continent continent = new Continent();
                    continent.setId(it.getIdFromQid());
                    continent.setName(it.name());
                    return continent;
                })
                .collect(
                        HashMap::new,
                        (map, element) -> map.put(element.getId(), element),
                        HashMap::putAll
                );
    }


    private Set<StaffRoleInMovie> getStaffRoleSet(Map<Integer, Staff> staffs, Movie movie, List<String> qids, String role, int limit) {
        return qids.stream()
                .mapToInt(qid -> Integer.parseInt(qid.substring(1)))
                .filter(staffs::containsKey)
                .limit(limit)
                .mapToObj(id -> {
                    Staff director = staffs.get(id);
                    StaffRoleInMovieId objId = new StaffRoleInMovieId();
                    objId.setMovie(movie.getId());
                    objId.setStaff(director.getId());
                    objId.setRole("director");

                    StaffRoleInMovie connector = new StaffRoleInMovie();
                    connector.setId(objId);
                    connector.setStaff(director);
                    connector.setMovie(movie);

                    return connector;
                })
                .collect(Collectors.toSet());
    }
}
