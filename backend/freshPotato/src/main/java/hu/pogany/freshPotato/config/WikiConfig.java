package hu.pogany.freshPotato.config;

import jakarta.persistence.MapsId;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.wiki")
@Validated
public record WikiConfig(
        @NotBlank(message = "no wikipedia api url was specified")
        @URL
        String apiUrl,
        @Min(1)
        int rateLimit,
        @NotBlank(message = "user agent is required for api requests")
        String userAgent,
        @NotBlank(message = "valid contact email is required for api requests")
        @Email(message = "valid contact email is required for api requests")
        String contactEmail) {
}
