package hu.pogany.freshPotato.controller;

import hu.pogany.freshPotato.dto.response.GenreDto;
import hu.pogany.freshPotato.service.GenreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/genre")
@Tag(name = "Genres", description = "Browse available movie genres")
public class GenreController {
    private final GenreService genreService;

    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }

    @GetMapping
    @Operation(summary = "List genres", description = "Returns all available movie genres")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Genres returned", content = @Content(array = @ArraySchema(schema = @Schema(implementation = GenreDto.class)))),
            @ApiResponse(responseCode = "500", description = "Database error", content = @Content(schema = @Schema(type = "string", example = "Database error")))
    })
    public List<GenreDto> getAll() {
        return genreService.getAll();
    }
}
