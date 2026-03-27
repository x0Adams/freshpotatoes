package hu.pogany.freshPotato.dto;

import jakarta.validation.constraints.*;
import org.springframework.validation.annotation.Validated;

@Validated
public record RegisterUserDto(
        @NotBlank
        @Email
        String email,
        @NotBlank
        String username,
        @NotBlank
        String genderName,
        @NotNull
        @Min(1)
        @Max(60)
        int age,
        @NotBlank
        String password
) {
}
