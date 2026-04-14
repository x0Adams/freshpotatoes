package hu.pogany.freshPotato.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "RefreshTokenRequest", description = "Request payload used to refresh or revoke an access token")
public record RefreshTokenDto(
        @NotBlank
        @Schema(description = "Valid refresh token string", example = "eyJhbGciOiJIUzI1NiJ9.refresh.payload", minLength = 1, requiredMode = Schema.RequiredMode.REQUIRED)
        String refreshToken
) {
}
