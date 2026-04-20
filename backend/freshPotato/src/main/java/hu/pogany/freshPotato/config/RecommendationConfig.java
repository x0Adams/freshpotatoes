package hu.pogany.freshPotato.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.recommendation")
@Validated
public record RecommendationConfig(
        @NotBlank
        @URL
        String apiUrl,
        @Min(1)
        @Max(100)
        int defaultCount
) {
}

