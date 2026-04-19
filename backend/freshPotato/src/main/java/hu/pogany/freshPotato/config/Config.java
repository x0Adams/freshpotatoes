package hu.pogany.freshPotato.config;

import io.github.bucket4j.BlockingBucket;
import io.github.bucket4j.Bucket;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static java.time.Duration.ofSeconds;

@Import(SecurityConfig.class)
@Configuration
public class Config {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("freshpotatoes docs")
                        .version("in development")
                        .description("Ádám Győri and Nagy Richárd's final project")
                )
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    @Bean("wikiLimiter")
    public BlockingBucket rateLimiter(WikiConfig config){
        return Bucket.builder()
                .addLimit(limit -> limit
                        .capacity(config.rateLimit())
                        .refillGreedy(config.rateLimit(), ofSeconds(1))
                        .initialTokens(config.rateLimit())
                )
                .build()
                .asBlocking();
    }

    @Bean("youtubeLimiter")
    public BlockingBucket youtubeLimiter(YoutubeConfig youtubeConfig) {
        ZoneId pacific = ZoneId.of("America/Los_Angeles");

        // Next midnight in Pacific Time (handles DST correctly)
        Instant nextPacificMidnight = ZonedDateTime.now(pacific)
                .toLocalDate()
                .plusDays(1)
                .atStartOfDay(pacific)
                .toInstant();


        return Bucket.builder().addLimit( limit -> limit
                .capacity(youtubeConfig.dailyLimit())
                .refillIntervallyAligned(
                        youtubeConfig.dailyLimit(),
                        Duration.ofDays(1),
                        nextPacificMidnight
                )
                .initialTokens(youtubeConfig.dailyLimit())
        ).build().asBlocking();
    }
}
