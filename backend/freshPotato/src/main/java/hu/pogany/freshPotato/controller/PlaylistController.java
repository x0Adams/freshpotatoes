package hu.pogany.freshPotato.controller;

import hu.pogany.freshPotato.dto.playlist.request.AddMovieToPlaylistRequestDto;
import hu.pogany.freshPotato.dto.playlist.request.ChangePlaylistVisibilityRequestDto;
import hu.pogany.freshPotato.dto.playlist.request.CreatePlaylistRequestDto;
import hu.pogany.freshPotato.dto.playlist.request.RenamePlaylistRequestDto;
import hu.pogany.freshPotato.dto.playlist.response.PlaylistDetailsDto;
import hu.pogany.freshPotato.dto.playlist.response.PlaylistOwnerNameDto;
import hu.pogany.freshPotato.service.PlaylistService;
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
import jakarta.validation.constraints.Min;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/playlist")
@Validated
@Tag(name = "Playlists", description = "Playlist management and movie list operations")
public class PlaylistController {

    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @PostMapping("/secure")
    @Operation(summary = "Create playlist", description = "Creates a new playlist for the authenticated user")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Playlist created", content = @Content(schema = @Schema(implementation = PlaylistOwnerNameDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid playlist payload", content = @Content(schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content(schema = @Schema(type = "string", example = "Unauthorized")))
    })
    public PlaylistOwnerNameDto createPlaylist(@Valid @RequestBody CreatePlaylistRequestDto request,
                                               @AuthenticationPrincipal Jwt jwt) {
        return playlistService.createPlaylist(getUserId(jwt), request);
    }

    @PatchMapping("/secure/{playlistId}/name")
    @Operation(summary = "Rename own playlist", description = "Renames a playlist owned by the authenticated user")
    @SecurityRequirement(name = "bearerAuth")
    public PlaylistOwnerNameDto renamePlaylist(@PathVariable @Min(1) int playlistId,
                                               @Valid @RequestBody RenamePlaylistRequestDto request,
                                               @AuthenticationPrincipal Jwt jwt) {
        return playlistService.renamePlaylist(getUserId(jwt), playlistId, request);
    }

    @DeleteMapping("/secure/{playlistId}")
    @Operation(summary = "Delete playlist", description = "Deletes a playlist owned by the authenticated user or by an admin")
    @SecurityRequirement(name = "bearerAuth")
    public void deletePlaylist(@PathVariable @Min(1) int playlistId,
                               @AuthenticationPrincipal Jwt jwt) {
        playlistService.deletePlaylist(getUserId(jwt), isAdmin(jwt), playlistId);
    }

    @PatchMapping("/secure/{playlistId}/visibility")
    @Operation(summary = "Change playlist visibility", description = "Changes playlist visibility for the owner or admin")
    @SecurityRequirement(name = "bearerAuth")
    public PlaylistOwnerNameDto changeVisibility(@PathVariable @Min(1) int playlistId,
                                                 @Valid @RequestBody ChangePlaylistVisibilityRequestDto request,
                                                 @AuthenticationPrincipal Jwt jwt) {
        return playlistService.changeVisibility(getUserId(jwt), isAdmin(jwt), playlistId, request);
    }

    @DeleteMapping("/admin/{playlistId}")
    @Operation(summary = "Admin delete playlist", description = "Deletes any playlist as admin")
    @SecurityRequirement(name = "bearerAuth")
    public void adminDeletePlaylist(@PathVariable @Min(1) int playlistId,
                                    @AuthenticationPrincipal Jwt jwt) {
        playlistService.deletePlaylist(getUserId(jwt), true, playlistId);
    }

    @PatchMapping("/admin/{playlistId}/visibility")
    @Operation(summary = "Admin change playlist visibility", description = "Changes visibility of any playlist as admin")
    @SecurityRequirement(name = "bearerAuth")
    public PlaylistOwnerNameDto adminChangeVisibility(@PathVariable @Min(1) int playlistId,
                                                      @Valid @RequestBody ChangePlaylistVisibilityRequestDto request,
                                                      @AuthenticationPrincipal Jwt jwt) {
        return playlistService.changeVisibility(getUserId(jwt), true, playlistId, request);
    }

    @GetMapping
    @Operation(summary = "Query playlist owner and name", description = "Returns playlists by owner and optional name filter with owner and playlist name only")
    @ApiResponse(responseCode = "200", description = "Playlists returned", content = @Content(array = @ArraySchema(schema = @Schema(implementation = PlaylistOwnerNameDto.class))))
    public List<PlaylistOwnerNameDto> queryPlaylists(
            @Parameter(description = "Owner user id", example = "7") @RequestParam @Min(1) int ownerId,
            @Parameter(description = "Playlist name filter (contains, case-insensitive)", example = "watch") @RequestParam(defaultValue = "") String name,
            @AuthenticationPrincipal Jwt jwt) {
        return playlistService.getPlaylistsByOwnerAndName(getUserIdOrNull(jwt), isAdmin(jwt), ownerId, name);
    }

    @GetMapping("/{playlistId}")
    @Operation(summary = "Get full playlist details", description = "Returns full playlist information including visibility and movies")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Playlist returned", content = @Content(schema = @Schema(implementation = PlaylistDetailsDto.class))),
            @ApiResponse(responseCode = "404", description = "Playlist not found", content = @Content(schema = @Schema(type = "string", example = "Playlist not found")))
    })
    public PlaylistDetailsDto getPlaylistDetails(@PathVariable @Min(1) int playlistId,
                                                 @AuthenticationPrincipal Jwt jwt) {
        return playlistService.getPlaylistDetails(getUserIdOrNull(jwt), isAdmin(jwt), playlistId);
    }

    @PostMapping("/secure/{playlistId}/movies")
    @Operation(summary = "Add movie to own playlist", description = "Adds a movie to a playlist owned by the authenticated user")
    @SecurityRequirement(name = "bearerAuth")
    public PlaylistDetailsDto addMovieToPlaylist(@PathVariable @Min(1) int playlistId,
                                                 @Valid @RequestBody AddMovieToPlaylistRequestDto request,
                                                 @AuthenticationPrincipal Jwt jwt) {
        return playlistService.addMovieToPlaylist(getUserId(jwt), playlistId, request.movieId());
    }

    @DeleteMapping("/secure/{playlistId}/movies/{movieId}")
    @Operation(summary = "Remove movie from own playlist", description = "Removes a movie from a playlist owned by the authenticated user")
    @SecurityRequirement(name = "bearerAuth")
    public PlaylistDetailsDto removeMovieFromPlaylist(@PathVariable @Min(1) int playlistId,
                                                      @PathVariable @Min(1) int movieId,
                                                      @AuthenticationPrincipal Jwt jwt) {
        return playlistService.removeMovieFromPlaylist(getUserId(jwt), playlistId, movieId);
    }

    private Integer getUserIdOrNull(Jwt jwt) {
        if (jwt == null) {
            return null;
        }
        return getUserId(jwt);
    }

    private int getUserId(Jwt jwt) {
        Object uid = jwt.getClaim("uid");
        if (uid instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(uid));
    }

    private boolean isAdmin(Jwt jwt) {
        if (jwt == null) {
            return false;
        }

        List<String> authorities = jwt.getClaimAsStringList("authorities");
        return authorities != null && authorities.contains("ROLE_ADMIN");
    }
}

