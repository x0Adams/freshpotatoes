package hu.pogany.freshPotato.repository;

import hu.pogany.freshPotato.entity.Review;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

public interface ReviewRepository extends GenericRateRepository<Review>{
	@Override
	@EntityGraph(attributePaths = {"user", "movie", "movie.genres"})
	List<Review> findByUserId(int userId);

	@Override
	@EntityGraph(attributePaths = {"user", "movie", "movie.genres"})
	List<Review> findByMovieId(int movieId);
}
