package hu.notulonme.MovieMiner.repository.mongo;

import hu.notulonme.MovieMiner.entity.dto.Staff;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StaffRepository extends MongoRepository<Staff, String> {
}
