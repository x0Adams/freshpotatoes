package hu.pogany.freshPotato.controller;

import hu.pogany.freshPotato.dto.MovieDto;
import hu.pogany.freshPotato.dto.SearchMovieDto;
import hu.pogany.freshPotato.service.MovieService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;

@RestController
public class MovieController {
    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/api/movie/search/{name}")
    public List<SearchMovieDto> searchMovieName(@PathVariable String name) {
        return movieService.searchForName(name);
    }

    @GetMapping("/api/movie/{id}")
    public MovieDto getMovie(@PathVariable int id) {
        return movieService.getMovie(id);
    }

    @GetMapping("/api/movie/")
    public List<SearchMovieDto> randomMovies() {
        return movieService.randomMovies();
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity handleExcpetion(EntityNotFoundException e){
        return ResponseEntity.status(404).body(e.getMessage());
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity handleSqlException(SQLException e) {
        return ResponseEntity.status(500).build();
    }

}
