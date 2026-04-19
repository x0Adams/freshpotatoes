package hu.pogany.freshPotato.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "LoginRequest", description = "Request payload for user login")
public record LoginDto(
        @NotBlank
        @Schema(description = "Username used for login", example = "john_doe", minLength = 1, requiredMode = Schema.RequiredMode.REQUIRED)
        String username,
        @NotBlank
        @Schema(description = "Raw account password", example = "StrongPassword123!", minLength = 1, requiredMode = Schema.RequiredMode.REQUIRED)
        String password
) {
}
