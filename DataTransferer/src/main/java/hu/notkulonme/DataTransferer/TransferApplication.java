package hu.notkulonme.DataTransferer;


import hu.notkulonme.DataTransferer.entity.*;
import hu.notkulonme.DataTransferer.entity.dto.*;
import hu.notkulonme.DataTransferer.repository.MovieRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TransferApplication {
    private static final Logger log = LoggerFactory.getLogger(TransferApplication.class);
    private final MongoTemplate mongo;
    private final MovieRepository movies;
    private final int movieBatchSize;

    public TransferApplication(MongoTemplate mongo,
                               MovieRepository movies,
                               @Value("${transfer.movie-batch-size:1000}") int movieBatchSize) {
        this.mongo = mongo;
        this.movies = movies;
        this.movieBatchSize = Math.max(1, movieBatchSize);
    }

    public void transfer() {
        log.info("Read continents from mongodb");
        Map<Integer, Continent> continents = mongo.findAll(ContinentMongo.class).stream()
                .map(ContinentMongo::toEntity)
                .filter(entity -> entity.getId() > 0)
                .collect(
                        HashMap::new,
                        (map, element) -> map.put(element.getId(), element),
                        HashMap::putAll
                );
        continents.putIfAbsent(0, ContinentMongo.defaultEntity());

        log.info("Read countries from mongodb");
        Map<Integer, Country> countries = mongo.findAll(CountryMongo.class).stream()
                .map(it -> it.toEntity(continents))
                .filter(entity -> entity.getId() > 0)
                .collect(
                        HashMap::new,
                        (map, element) -> map.put(element.getId(), element),
                        HashMap::putAll
                );
        countries.putIfAbsent(0, CountryMongo.defaultEntity(Set.of(continents.get(0))));

        log.info("Read genders from mongodb");
        Map<Integer, Gender> genders = mongo.findAll(GenderMongo.class).stream()
                .map(GenderMongo::toEntity)
                .filter(entity -> entity.getId() > 0)
                .collect(
                        HashMap::new,
                        (map, element) -> map.put(element.getId(), element),
                        HashMap::putAll
                );
        genders.putIfAbsent(0, GenderMongo.defaultEntity());

        log.info("Read genres from mongodb");
        Map<Integer, Genre> genres = mongo.findAll(GenreMongo.class).stream()
                .map(GenreMongo::toEntity)
                .filter(entity -> entity.getId() > 0)
                .collect(
                        HashMap::new,
                        (map, element) -> map.put(element.getId(), element),
                        HashMap::putAll
                );
        genres.putIfAbsent(0, GenreMongo.defaultEntity());

        log.info("Read staffs from mongodb");
        Map<Integer, Staff> staffs = mongo.findAll(StaffMongo.class).stream()
                .map(it -> it.toEntity(genders, countries))
                .filter(entity -> entity.getId() > 0)
                .collect(
                        HashMap::new,
                        (map, element) -> map.put(element.getId(), element),
                        HashMap::putAll
                );
        log.info("start transfer");
        transferMovies(genres, staffs, countries);
    }

    private void transferMovies(Map<Integer, Genre> genres, Map<Integer, Staff> staffs, Map<Integer, Country> countries) {
        List<Movie> batch = new ArrayList<>(movieBatchSize);
        int scanned = 0;
        int persisted = 0;

        try (var cursor = mongo.stream(new Query(), FilmMongo.class)) {
            Iterator<FilmMongo> iterator = cursor.iterator();
            while (iterator.hasNext()) {
                scanned++;
                Movie movie = iterator.next().toEntity(genres, staffs, countries);
                if (movie.getId() <= 0) {
                    continue;
                }

                batch.add(movie);
                if (batch.size() >= movieBatchSize) {
                    persisted += persistMovieBatch(batch);
                    if (persisted % (movieBatchSize * 10) == 0) {
                        log.info("Persisted {} movies so far", persisted);
                    }
                }
                log.debug("scanned={}", scanned);
            }
        }

        persisted += persistMovieBatch(batch);
        log.info("Movie transfer completed: scanned={}, persisted={}, batchSize={}", scanned, persisted, movieBatchSize);
    }

    private int persistMovieBatch(List<Movie> batch) {
        if (batch.isEmpty()) {
            return 0;
        }
        int size = batch.size();
        log.debug("persisting {} movies", size);
        movies.saveAllAndFlush(batch);
        batch.clear();
        return size;
    }
}
