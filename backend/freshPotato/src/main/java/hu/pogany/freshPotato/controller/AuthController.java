package hu.pogany.freshPotato.controller;

import hu.pogany.freshPotato.dto.response.LoginDto;
import hu.pogany.freshPotato.dto.response.RefreshTokenDto;
import hu.pogany.freshPotato.dto.RegisterUserDto;
import hu.pogany.freshPotato.dto.response.TokensDto;
import hu.pogany.freshPotato.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityExistsException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Login, registration and token lifecycle endpoints")
public class AuthController {
    private final AuthenticationService authService;

    public AuthController(AuthenticationService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticates with username/password and returns a short-lived JWT plus refresh token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(schema = @Schema(implementation = TokensDto.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(type = "string", example = "Invalid user credentials")))
    })
    public TokensDto login(@RequestBody @Valid LoginDto loginData) throws AuthenticationException {
        TokensDto tokens = authService.login(loginData.username(), loginData.password());
        return tokens;
    }

    @PostMapping("/register")
    @Operation(summary = "Register user", description = "Creates a new enabled user account")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User created"),
            @ApiResponse(responseCode = "400", description = "Registration failed (e.g. duplicate user or invalid data)", content = @Content(schema = @Schema(type = "string", example = "Username already exists")))
    })
    public ResponseEntity<String> registerUser(@RequestBody @Valid RegisterUserDto userData) {
        try {
            authService.register(userData);
        } catch (EntityExistsException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.ok().body("User is created");
    }

    @PostMapping("/admin/register")
    @Operation(summary = "Register admin", description = "Creates a new admin account")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Admin created"),
            @ApiResponse(responseCode = "400", description = "Registration failed (e.g. duplicate user or invalid data)", content = @Content(schema = @Schema(type = "string", example = "Username already exists")))
    })
    public ResponseEntity<String> registerAdmin(@RequestBody @Valid RegisterUserDto userData) {
        try {
            authService.registerAdmin(userData);
        } catch (EntityExistsException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.ok().body("Admin is created");
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh tokens", description = "Marks the provided refresh token as used and returns a new JWT and refresh token pair")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens refreshed", content = @Content(schema = @Schema(implementation = TokensDto.class))),
            @ApiResponse(responseCode = "401", description = "Refresh token is invalid or expired", content = @Content(schema = @Schema(type = "string", example = "Expired token")))
    })
    public TokensDto refresh(@RequestBody @Valid RefreshTokenDto refreshToken) throws AuthenticationException {
        return authService.refresh(refreshToken.refreshToken());
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Invalidates a refresh token so it can no longer be used")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logout successful"),
            @ApiResponse(responseCode = "401", description = "Refresh token is invalid or already used", content = @Content(schema = @Schema(type = "string", example = "Token already used")))
    })
    public void logout(@RequestBody @Valid RefreshTokenDto refreshToken) throws AuthenticationException {
        authService.logout(refreshToken.refreshToken());
    }

    @GetMapping("/me")
    @Operation(summary = "Current principal claims", description = "Returns claims from the authenticated JWT")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Claims returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content(schema = @Schema(type = "string", example = "Unauthorized")))
    })
    public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
        return jwt.getClaims();
    }


}
