package hu.pogany.freshPotato.controller;

import hu.pogany.freshPotato.dto.response.MovieDto;
import hu.pogany.freshPotato.dto.response.SearchMovieDto;
import hu.pogany.freshPotato.service.MovieService;
import hu.pogany.freshPotato.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movie")
@Validated
@Tag(name = "Movies", description = "Movie browsing and discovery endpoints")
public class MovieController {
    private final MovieService movieService;
    private final UserService userService;

    public MovieController(MovieService movieService, UserService userService) {
        this.movieService = movieService;
        this.userService = userService;
    }

    @GetMapping("/search/{name}")
    @Operation(summary = "Search movie by name prefix", description = "Returns up to 5 movies where name starts with the provided value, preferring shorter name matches")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Matching movies returned", content = @Content(schema = @Schema(implementation = SearchMovieDto.class))),
            @ApiResponse(responseCode = "404", description = "No movies found for the given prefix", content = @Content(schema = @Schema(type = "string", example = "No movies found for name: The")))
    })
    public List<SearchMovieDto> searchMovieName(@Parameter(description = "Movie name prefix", example = "The") @PathVariable String name) {
        return movieService.searchForName(name);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get movie details", description = "Returns movie details and records a view event for the authenticated user or anonymous visitor")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movie found", content = @Content(schema = @Schema(implementation = MovieDto.class))),
            @ApiResponse(responseCode = "404", description = "Movie not found", content = @Content(schema = @Schema(type = "string", example = "Movie not found")))
    })
    public MovieDto getMovie(@Parameter(description = "Movie id", example = "42") @PathVariable int id, @AuthenticationPrincipal Jwt token) {
        int userId;
        if (token == null) {
            userId = -1;
        } else {
            userId = Integer.parseInt(token.getClaimAsString("id"));
        }

        return movieService.getMovieSaveView(id, userId);
    }

    @GetMapping("/random")
    @Operation(summary = "Get random movies", description = "Returns a random selection of movies for discovery")
    @ApiResponse(responseCode = "200", description = "Random movies returned", content = @Content(schema = @Schema(implementation = SearchMovieDto.class)))
    public List<SearchMovieDto> randomMovies() {
        return movieService.randomMovies();
    }

    @GetMapping
    @Operation(summary = "Get popular movies", description = "Returns movies sorted by popularity in a paginated form")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Popular movies returned", content = @Content(schema = @Schema(implementation = SearchMovieDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid paging parameters", content = @Content(schema = @Schema(type = "string", example = "size must be less than or equal to 100")))
    })
    public List<SearchMovieDto> popularMovies(
            @Parameter(description = "Zero-based page index", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 100)", example = "30") @RequestParam(defaultValue = "30") @Max(100) int size)
    {
        return movieService.findPopularMovies(page, size);
    }


}
