package hu.pogany.freshPotato.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app.jwt")
public record TokenConfig(
        @NotNull
        Duration accessTokenTtl,
        @NotNull
        Duration refreshTokenTtl
) {
}
