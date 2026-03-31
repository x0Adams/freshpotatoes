package hu.pogany.freshPotato.dto;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.validation.annotation.Validated;

@Validated
public record LoginDto(
        @NotEmpty
        String username,
        @NotEmpty
        String password
) {
}
