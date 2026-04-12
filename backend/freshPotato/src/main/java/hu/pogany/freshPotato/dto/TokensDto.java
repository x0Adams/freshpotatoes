package hu.pogany.freshPotato.dto;

public record TokensDto(
        String jwtToken,
        String refreshToken
) {
}
