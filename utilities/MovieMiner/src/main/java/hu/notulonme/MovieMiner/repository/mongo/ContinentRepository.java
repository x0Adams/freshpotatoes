package hu.notulonme.MovieMiner.repository.mongo;

import hu.notulonme.MovieMiner.entity.dto.Continent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContinentRepository extends MongoRepository<Continent, String > {

}
