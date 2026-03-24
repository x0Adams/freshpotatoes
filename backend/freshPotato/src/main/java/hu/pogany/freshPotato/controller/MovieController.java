package hu.pogany.freshPotato.controller;

import hu.pogany.freshPotato.dto.MovieDto;
import hu.pogany.freshPotato.dto.SearchMovieDto;
import hu.pogany.freshPotato.service.MovieService;
import hu.pogany.freshPotato.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.Max;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/movie")
@Validated
public class MovieController {
    private final MovieService movieService;
    private final UserService userService;

    public MovieController(MovieService movieService, UserService userService) {
        this.movieService = movieService;
        this.userService = userService;
    }

    @GetMapping("/search/{name}")
    public List<SearchMovieDto> searchMovieName(@PathVariable String name) {
        return movieService.searchForName(name);
    }

    @GetMapping("/{id}")
    public MovieDto getMovie(@PathVariable int id, @AuthenticationPrincipal UserDetails user) {
        int userId;
        if (user == null) {
            userId = -1;
        } else {
            try {
                userId = userService.getIdByUserName(user.getUsername());
            } catch (EntityNotFoundException e) {
                userId = -1;
            }
        }

        return movieService.getMovieSaveView(id, userId);
    }

    @GetMapping("/random")
    public List<SearchMovieDto> randomMovies() {
        return movieService.randomMovies();
    }

    @GetMapping
    public List<SearchMovieDto> popularMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") @Max(100) int size)
    {
        return movieService.findPopularMovies(page, size);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity handleExcpetion(EntityNotFoundException e) {
        return ResponseEntity.status(404).body(e.getMessage());
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity handleSqlException(SQLException e) {
        return ResponseEntity.status(500).build();
    }

}
