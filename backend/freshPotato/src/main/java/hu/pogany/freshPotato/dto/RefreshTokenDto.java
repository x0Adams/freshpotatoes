package hu.pogany.freshPotato.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record RefreshTokenDto(
        @NotBlank
        String refreshToken
) {
}
