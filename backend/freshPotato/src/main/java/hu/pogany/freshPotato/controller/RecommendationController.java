package hu.pogany.freshPotato.controller;

import hu.pogany.freshPotato.dto.response.SearchMovieDto;
import hu.pogany.freshPotato.service.JwtService;
import hu.pogany.freshPotato.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recommend")
@Tag(name = "Recommendations", description = "Personalized movie recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/secure/me")
    @Operation(
            summary = "Get recommendations for current user",
            description = "Returns personalized recommendations based on the authenticated user's interactions."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recommendations returned", content = @Content(array = @ArraySchema(schema = @Schema(implementation = SearchMovieDto.class)))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content(schema = @Schema(type = "string", example = "Unauthorized"))),
            @ApiResponse(responseCode = "404", description = "No previous interaction for recommending movies", content = @Content(schema = @Schema(type = "string", example = "No previous interaction for recommending movies"))),
            @ApiResponse(responseCode = "500", description = "Recommendation microservice is currently down", content = @Content(schema = @Schema(type = "string", example = "Recommendation service unavailable")))
    })
    public List<SearchMovieDto> recommendForMe(@AuthenticationPrincipal Jwt jwt) {
        return recommendationService.getRecommendations(JwtService.getUserId(jwt));
    }

    @GetMapping("/admin/{userid}")
    @Operation(
            summary = "Admin recommendations for a user",
            description = "Returns personalized recommendations for the specified user id. Admin access only."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recommendations returned", content = @Content(array = @ArraySchema(schema = @Schema(implementation = SearchMovieDto.class)))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content(schema = @Schema(type = "string", example = "Unauthorized"))),
            @ApiResponse(responseCode = "403", description = "Authenticated but not allowed", content = @Content(schema = @Schema(type = "string", example = "Forbidden"))),
            @ApiResponse(responseCode = "404", description = "No previous interaction for recommending movies", content = @Content(schema = @Schema(type = "string", example = "No previous interaction for recommending movies"))),
            @ApiResponse(responseCode = "500", description = "Recommendation microservice is currently down", content = @Content(schema = @Schema(type = "string", example = "Recommendation service unavailable")))
    })
    public List<SearchMovieDto> adminRecommender(
            @Parameter(description = "Target user id", example = "42") @PathVariable int userid
    ) {
        return recommendationService.getRecommendations(userid);
    }
}
