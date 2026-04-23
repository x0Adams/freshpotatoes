package hu.pogany.freshPotato.repository;

import hu.pogany.freshPotato.entity.Rate;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

public interface RateRepository extends GenericRateRepository<Rate>{
	@Override
	@EntityGraph(attributePaths = {"user", "movie", "movie.genres"})
	List<Rate> findByUserId(int userId);

	@Override
	@EntityGraph(attributePaths = {"user", "movie"})
	List<Rate> findByMovieId(int movieId);
}
