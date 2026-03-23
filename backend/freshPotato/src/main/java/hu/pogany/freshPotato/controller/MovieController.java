package hu.pogany.freshPotato.controller;

import hu.pogany.freshPotato.dto.MovieDto;
import hu.pogany.freshPotato.dto.SearchMovieDto;
import hu.pogany.freshPotato.service.MovieService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/movie")
public class MovieController {
    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/search/{name}")
    public List<SearchMovieDto> searchMovieName(@PathVariable String name) {
        return movieService.searchForName(name);
    }

    @GetMapping("/api/movie/{id}")
    public MovieDto getMovie(@PathVariable int id) {
        //TODO: implement logic for counting visits
        return movieService.getMovie(id);
    }

    @GetMapping("/random")
    public List<SearchMovieDto> randomMovies() {
        return movieService.randomMovies();
    }

    @GetMapping
    public List<SearchMovieDto> popularMovies(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "30") int size) {
        return movieService.findPopularMovies(page, size);
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
