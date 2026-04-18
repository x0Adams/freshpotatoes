package hu.pogany.freshPotato.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.youtube")
@Validated
public record YoutubeConfig (
    @NotBlank
    String apiUrl,
    @NotBlank
    String apiKey,
    @Min(1)
    long dailyLimit,
    @Min(1)
    int queryCost
){}
