package hu.pogany.freshPotato.dto.response;

public record TokensDto(
        String jwtToken,
        String refreshToken
) {
}
