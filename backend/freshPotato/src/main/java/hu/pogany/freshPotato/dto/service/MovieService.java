package hu.pogany.freshPotato.dto.service;

import hu.pogany.freshPotato.dao.MovieRepository;
import hu.pogany.freshPotato.dto.entity.*;
import hu.pogany.freshPotato.entity.Movie;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class MovieService {
    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public List<SearchMovieDto> searchForName(String name) {
        List<Movie> movies = movieRepository.findTop5ByNameIsLike(name + "%", JpaSort.unsafe("LENGTH(name)"));
        return movies.stream().map(movie -> new SearchMovieDto(movie.getUuid(), movie.getName(), movie.getPosterPath())).toList();
    }

    public MovieDto getMovie(String uuid) {
        Movie movie = movieRepository.findById(uuid).orElseThrow(() -> new EntityNotFoundException("Movie not found"));

        Set<MovieActorDto> actorDtos = movie.getActors().stream().map(actor -> new MovieActorDto(actor.getId(), actor.getName())).collect(Collectors.toSet());
        Set<MovieDirectorDto> directorDtos = movie.getDirectors().stream().map(director -> new MovieDirectorDto(director.getId(), director.getName())).collect(Collectors.toSet());
        Set<GenreDto> genres = movie.getGenres().stream().map(genre -> new GenreDto(genre.getName())).collect(Collectors.toSet());

        return new MovieDto(
                movie.getUuid(),
                movie.getName(),
                movie.getPosterPath(),
                movie.getDuration(),
                movie.getReleaseDate(),
                movie.getYoutubeMovie(),
                movie.getGoogleKnowledgeGraph(),
                movie.getCountryOfOrigin(),
                movie.getTrailer(),
                genres,
                actorDtos,
                directorDtos
        );
    }

    public List<SearchMovieDto> randomMovies() {
        return movieRepository.findRandom().stream().map(movie -> new SearchMovieDto(movie.getUuid(), movie.getName(), movie.getPosterPath())).toList();
    }
}
