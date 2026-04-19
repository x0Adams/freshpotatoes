package hu.pogany.freshPotato.controller;

import hu.pogany.freshPotato.dto.rate.DeleteRateDto;
import hu.pogany.freshPotato.dto.rate.DeleteRateRequestDto;
import hu.pogany.freshPotato.dto.rate.GenericRateDto;
import hu.pogany.freshPotato.dto.rate.RateRequestDto;
import hu.pogany.freshPotato.entity.Rate;
import hu.pogany.freshPotato.service.RateService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rate")
@Tag(name = "Ratings", description = "Create, delete and list movie ratings")
public class RateController extends AbstractRateController<Integer, Rate> {
    public RateController(RateService rateService) {
        super(rateService);
    }


    @DeleteMapping("/admin")
    @Operation(summary = "Admin delete rating", description = "Deletes a specific user's rating for a movie")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rating deleted"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content(schema = @Schema(type = "string", example = "Unauthorized"))),
            @ApiResponse(responseCode = "403", description = "Authenticated but not allowed", content = @Content(schema = @Schema(type = "string", example = "Forbidden")))
    })
    public void adminDeleteRate(@Valid @RequestBody  DeleteRateDto dto) {
        super.adminDeleteRate(dto);
    }

    @DeleteMapping("/secure")
    @Operation(summary = "Delete own rating", description = "Deletes the authenticated user's rating for the given movie")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rating deleted"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content(schema = @Schema(type = "string", example = "Unauthorized")))
    })
    public void deleteRate(@Valid @RequestBody DeleteRateRequestDto deleteRateRequestDto, @AuthenticationPrincipal Jwt jwt) {
        super.deleteRate(deleteRateRequestDto, jwt);
    }

    @PostMapping("/secure")
    @Operation(summary = "Create or update own rating", description = "Creates a new rating or updates an existing rating for the authenticated user and target movie")
    @SecurityRequirement(name = "bearerAuth")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(schema = @Schema(implementation = RateRequestDto.class))
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rating saved"),
            @ApiResponse(responseCode = "400", description = "Invalid rating payload", content = @Content(schema = @Schema(type = "string", example = "Rate value must be between 1 and 5"))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content(schema = @Schema(type = "string", example = "Unauthorized"))),
            @ApiResponse(responseCode = "404", description = "User or movie not found", content = @Content(schema = @Schema(type = "string", example = "Movie not found")))
    })
    public ResponseEntity<String> rateMovie(@Valid @RequestBody RateRequestDto rate, @AuthenticationPrincipal Jwt jwt) {
        return super.rateMovie(rate, jwt);
    }

    @GetMapping
    @Override
    @Operation(summary = "List ratings by user", description = "Returns all ratings created by the specified user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ratings returned", content = @Content(array = @ArraySchema(schema = @Schema(implementation = GenericRateDto.class)))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(type = "string", example = "User doesn't exists")))
    })
    public List<GenericRateDto<Integer>> getAllByUser(@Parameter(description = "User id", example = "12") @RequestParam int userid) {
        return super.getAllByUser(userid);
    }

    @GetMapping("/{movieId}")
    @Override
    @Operation(summary = "List ratings for movie", description = "Returns all ratings for the specified movie")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ratings returned", content = @Content(array = @ArraySchema(schema = @Schema(implementation = GenericRateDto.class)))),
            @ApiResponse(responseCode = "404", description = "Movie not found", content = @Content(schema = @Schema(type = "string", example = "no movie with this id in the database")))
    })
    public List<GenericRateDto<Integer>> getAllByMovie(@Parameter(description = "Movie id", example = "42") @PathVariable int movieId) {
        return super.getAllByMovie(movieId);
    }
}
