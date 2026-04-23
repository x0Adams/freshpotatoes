package hu.pogany.freshPotato.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.rabbitmq")
@Validated
public record RabbitMessagingConfig(
        @NotBlank
        String ratingQueue
) {
}

