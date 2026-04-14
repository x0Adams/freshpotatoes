package hu.pogany.freshPotato.dto.response;

import lombok.Builder;

@Builder
public record UserDto(
        int id,
        String username,
        String email,
        int age
        ) {
}
