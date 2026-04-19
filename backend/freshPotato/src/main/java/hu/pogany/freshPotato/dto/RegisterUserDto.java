package hu.pogany.freshPotato.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(name = "RegisterUserRequest", description = "Request payload for registering a user")
public record RegisterUserDto(
        @NotBlank
        @Email
        @Schema(description = "User e-mail address", example = "user@example.com", format = "email", minLength = 1, requiredMode = Schema.RequiredMode.REQUIRED)
        String email,
        @NotBlank
        @Schema(description = "Unique username", example = "potatoFan", minLength = 1, requiredMode = Schema.RequiredMode.REQUIRED)
        String username,
        @NotBlank
        @Schema(description = "Gender name from reference data", example = "male", minLength = 1, requiredMode = Schema.RequiredMode.REQUIRED)
        String genderName,
        @Min(1)
        @Max(120)
        @Schema(description = "User age in years", example = "25", minimum = "1", maximum = "120", requiredMode = Schema.RequiredMode.REQUIRED)
        int age,
        @NotBlank
        @Schema(description = "Raw account password", example = "StrongPassword123!", minLength = 1, requiredMode = Schema.RequiredMode.REQUIRED)
        String password
) {
}
