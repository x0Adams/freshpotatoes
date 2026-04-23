package hu.notulonme.MovieMiner.repository.mongo;

import hu.notulonme.MovieMiner.entity.dto.Continent;
import hu.notulonme.MovieMiner.entity.dto.Country;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CountryRepository extends MongoRepository<Country, String > {
}
