package hu.pogany.freshPotato.dto;

import jakarta.validation.constraints.*;
import org.springframework.validation.annotation.Validated;

public record RegisterUserDto(
        @NotBlank
        @Email
        String email,
        @NotBlank
        String username,
        @NotBlank
        String genderName,
        @Min(1)
        @Max(120)
        int age,
        @NotBlank
        String password
) {
}
