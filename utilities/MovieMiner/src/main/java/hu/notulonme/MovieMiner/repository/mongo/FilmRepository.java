package hu.notulonme.MovieMiner.repository.mongo;

import hu.notulonme.MovieMiner.entity.dto.Film;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FilmRepository extends MongoRepository<Film, String > {
}
