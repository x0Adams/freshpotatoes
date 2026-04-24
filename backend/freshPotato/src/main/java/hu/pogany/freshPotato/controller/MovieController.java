package hu.pogany.freshPotato.controller;

import hu.pogany.freshPotato.dto.ModifyMovieDto;
import hu.pogany.freshPotato.dto.response.MovieDto;
import hu.pogany.freshPotato.dto.response.SearchMovieDto;
import hu.pogany.freshPotato.service.JwtService;
import hu.pogany.freshPotato.service.MovieService;
import hu.pogany.freshPotato.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
            @ApiResponse(responseCode = "200", description = "Matching movies returned", content = @Content(array = @ArraySchema(schema = @Schema(implementation = SearchMovieDto.class)))),
            @ApiResponse(responseCode = "404", description = "No movies found for the given prefix", content = @Content(schema = @Schema(type = "string", example = "No movies found for name: The")))
    })
    public List<SearchMovieDto> searchMovieName(@Parameter(description = "Movie name prefix", example = "The") @PathVariable String name) {
        return movieService.searchForName(name);
    }

    @GetMapping("/search")
    @Operation(
            summary = "Advanced movie search",
            description = "Searches movies by title relevance and optional staff/genre filters. Exact title matches come first, then prefix matches sorted by popularity."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Matching movies returned", content = @Content(array = @ArraySchema(schema = @Schema(implementation = SearchMovieDto.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid search parameters", content = @Content(schema = @Schema(type = "string", example = "Title must not be empty"))),
            @ApiResponse(responseCode = "404", description = "No movies found for search criteria", content = @Content(schema = @Schema(type = "string", example = "No movies found for the given search criteria")))
    })
    public List<SearchMovieDto> advancedSearch(
            @Parameter(description = "Movie title used for exact or prefix search", example = "The Matrix")
            @RequestParam String title,
            @Parameter(description = "Filter by staff names (repeat or comma-separated)", example = "Keanu Reeves")
            @RequestParam(required = false) List<String> staff,
            @Parameter(description = "Filter by genre names (repeat or comma-separated)", example = "Action")
            @RequestParam(required = false) List<String> genre,
            @Parameter(description = "Zero-based page index", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 100)", example = "30") @RequestParam(defaultValue = "30") @Max(100) int size
    ) {
        return movieService.advancedSearch(title, staff, genre, page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get movie details", description = "Returns movie details and records a view event for the authenticated user or anonymous visitor")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movie found", content = @Content(schema = @Schema(implementation = MovieDto.class))),
            @ApiResponse(responseCode = "404", description = "Movie not found", content = @Content(schema = @Schema(type = "string", example = "Movie not found")))
    })
    public MovieDto getMovie(@Parameter(description = "Movie id", example = "13417189") @PathVariable int id, @AuthenticationPrincipal Jwt token) {
        int userId;
        if (token == null) {
            userId = -1;
        } else {
            userId = JwtService.getUserId(token);
        }

        return movieService.getMovieSaveView(id, userId);
    }

    @GetMapping("/random")
    @Operation(summary = "Get random movies", description = "Returns a random selection of movies for discovery")
    @ApiResponse(responseCode = "200", description = "Random movies returned", content = @Content(array = @ArraySchema(schema = @Schema(implementation = SearchMovieDto.class))))
    public List<SearchMovieDto> randomMovies() {
        return movieService.randomMovies();
    }

    @GetMapping
    @Operation(summary = "Get popular movies", description = "Returns movies sorted by popularity in a paginated form")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Popular movies returned", content = @Content(array = @ArraySchema(schema = @Schema(implementation = SearchMovieDto.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid paging parameters", content = @Content(schema = @Schema(type = "string", example = "size must be less than or equal to 100")))
    })
    public List<SearchMovieDto> popularMovies(
            @Parameter(description = "Zero-based page index", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 100)", example = "30") @RequestParam(defaultValue = "30") @Max(100) int size)
    {
        return movieService.findPopularMovies(page, size);
    }

    @GetMapping("/genre/{genre}")
    @Operation(summary = "Get popular movies by genre", description = "Returns movies of the selected genre sorted by popularity in a paginated form")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Popular movies by genre returned", content = @Content(array = @ArraySchema(schema = @Schema(implementation = SearchMovieDto.class)))),
            @ApiResponse(responseCode = "404", description = "Genre does not exist or no movies found", content = @Content(schema = @Schema(type = "string")))
    })
    public List<SearchMovieDto> popularMoviesByGenre(
            @Parameter(description = "Genre name", example = "Action") @PathVariable String genre,
            @Parameter(description = "Zero-based page index", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 100)", example = "30") @RequestParam(defaultValue = "30") @Max(100) int size) {
        return movieService.findPopularMoviesByGenre(genre, page, size);
    }

    @PatchMapping("/admin/{id}")
    @Operation(summary = "Admin modify movie", description = "Modifies a movie by id as admin")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movie updated"),
            @ApiResponse(responseCode = "400", description = "Invalid movie payload", content = @Content(schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "404", description = "Movie not found", content = @Content(schema = @Schema(type = "string", example = "Movie not found")))
    })
    public void modifyMovie(@PathVariable @Min(1) int id, @Valid @RequestBody ModifyMovieDto dto) {
        movieService.modify(id, dto);
    }

    @DeleteMapping("/admin/{id}")
    @Operation(summary = "Admin delete movie", description = "Deletes a movie and all dependent links as admin")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movie deleted"),
            @ApiResponse(responseCode = "404", description = "Movie not found", content = @Content(schema = @Schema(type = "string", example = "Movie not found")))
    })
    public void deleteMovie(@PathVariable @Min(1) int id) {
        movieService.deleteMovie(id);
    }


}
