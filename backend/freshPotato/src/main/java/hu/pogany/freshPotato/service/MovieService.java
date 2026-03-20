package hu.pogany.freshPotato.service;

import hu.pogany.freshPotato.dto.*;
import hu.pogany.freshPotato.mapper.Mapper;
import hu.pogany.freshPotato.repository.MovieRepository;
import hu.pogany.freshPotato.entity.Movie;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class MovieService {
    private final MovieRepository movieRepository;
    private final Mapper mapper;

    public MovieService(MovieRepository movieRepository, Mapper mapper) {
        this.movieRepository = movieRepository;
        this.mapper = mapper;
    }

    public List<SearchMovieDto> searchForName(String name) {
        List<Movie> movies = movieRepository.findTop5ByNameIsLike(name + "%", JpaSort.unsafe("LENGTH(name)"));
        if (!movies.isEmpty())
            return mapper.toSearchMovieDtoList(movies);
        else
            throw new EntityNotFoundException("No movies found for name: " + name);
    }

    public MovieDto getMovie(int id) {
        Movie movie = movieRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Movie not found"));
        return mapper.toMovieDto(movie);
    }

    public List<SearchMovieDto> randomMovies() {
        return mapper.toSearchMovieDtoList(movieRepository.findRandom());
    }
}
