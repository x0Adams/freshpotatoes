package hu.notulonme.MovieMiner.repository.mongo;

import hu.notulonme.MovieMiner.entity.dto.Gender;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenderRepository extends MongoRepository<Gender, String > {
}
