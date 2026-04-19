package hu.pogany.freshPotato.controller;

import hu.pogany.freshPotato.dto.rate.DeleteRateDto;
import hu.pogany.freshPotato.dto.rate.DeleteRateRequestDto;
import hu.pogany.freshPotato.dto.rate.GenericRateDto;
import hu.pogany.freshPotato.dto.rate.ReviewRequestDto;
import hu.pogany.freshPotato.entity.Review;
import hu.pogany.freshPotato.service.ReviewService;
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
@RequestMapping("/api/review")
@Tag(name = "Reviews", description = "Create, delete and list movie reviews")
public class ReviewController extends AbstractRateController<String, Review> {

    public ReviewController(ReviewService rateService) {
        super(rateService);
    }

    @DeleteMapping("/admin")
    @Override
    @Operation(summary = "Admin delete review", description = "Deletes a specific user's review for a movie")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Review deleted"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content(schema = @Schema(type = "string", example = "Unauthorized"))),
            @ApiResponse(responseCode = "403", description = "Authenticated but not allowed", content = @Content(schema = @Schema(type = "string", example = "Forbidden")))
    })
    public void adminDeleteRate(@Valid @RequestBody DeleteRateDto dto) {
        super.adminDeleteRate(dto);
    }

    @DeleteMapping("/secure")
    @Override
    @Operation(summary = "Delete own review", description = "Deletes the authenticated user's review for the given movie")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Review deleted"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content(schema = @Schema(type = "string", example = "Unauthorized")))
    })
    public void deleteRate(@Valid @RequestBody DeleteRateRequestDto deleteRateRequestDto, @AuthenticationPrincipal Jwt jwt) {
        super.deleteRate(deleteRateRequestDto, jwt);
    }

    @PostMapping("/secure")
    @Operation(summary = "Create or update own review", description = "Creates a new review or updates an existing review for the authenticated user and target movie")
    @SecurityRequirement(name = "bearerAuth")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(schema = @Schema(implementation = ReviewRequestDto.class))
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Review saved"),
            @ApiResponse(responseCode = "400", description = "Invalid review payload", content = @Content(schema = @Schema(type = "string", example = "Review text must not be blank"))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content(schema = @Schema(type = "string", example = "Unauthorized"))),
            @ApiResponse(responseCode = "404", description = "User or movie not found", content = @Content(schema = @Schema(type = "string", example = "Movie not found")))
    })
    public ResponseEntity<String> rateMovie(@Valid @RequestBody ReviewRequestDto rate, @AuthenticationPrincipal Jwt jwt) {
        return super.rateMovie(rate, jwt);
    }

    @GetMapping
    @Override
    @Operation(summary = "List reviews by user", description = "Returns all reviews created by the specified user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reviews returned", content = @Content(array = @ArraySchema(schema = @Schema(implementation = GenericRateDto.class)))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(type = "string", example = "User doesn't exists")))
    })
    public List<GenericRateDto<String>> getAllByUser(@Parameter(description = "User id", example = "12") @RequestParam int userid) {
        return super.getAllByUser(userid);
    }

    @GetMapping("/{movieId}")
    @Override
    @Operation(summary = "List reviews for movie", description = "Returns all reviews for the specified movie")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reviews returned", content = @Content(array = @ArraySchema(schema = @Schema(implementation = GenericRateDto.class)))),
            @ApiResponse(responseCode = "404", description = "Movie not found", content = @Content(schema = @Schema(type = "string", example = "no movie with this id in the database")))
    })
    public List<GenericRateDto<String>> getAllByMovie(@Parameter(description = "Movie id", example = "42") @PathVariable int movieId) {
        return super.getAllByMovie(movieId);
    }
}
