package hu.pogany.freshPotato.dto.service;

import hu.pogany.freshPotato.dao.MovieRepository;
import hu.pogany.freshPotato.dto.entity.SearchMovieDto;
import hu.pogany.freshPotato.entity.Movie;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MovieService {
    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public List<SearchMovieDto> searchForName(String name) {
        List<Movie> movies = movieRepository.findTop5ByNameIsLike(name+"%", JpaSort.unsafe("LENGTH(name)"));
        return movies.stream().map(movie -> new SearchMovieDto(movie.getUuid(), movie.getName(), movie.getPosterPath())).toList();
    }
}
