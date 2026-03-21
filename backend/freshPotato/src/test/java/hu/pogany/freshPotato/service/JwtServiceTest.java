package hu.pogany.freshPotato.service;

import hu.pogany.freshPotato.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    @Test
    void generateAccessTokenUsesConfiguredThreeMinuteLifetime() {
        JwtProperties props = new JwtProperties();
        props.setSecret("veryLongVeryyysecretServerSecret");
        props.setAccessTokenTtl(Duration.ofMinutes(3));
        props.setRefreshTokenTtl(Duration.ofDays(10));

        JwtService jwtService = new JwtService(props);
        UserDetails user = User.withUsername("alice")
                .password("pw")
                .authorities("ROLE_USER")
                .build();

        String token = jwtService.generateAccessToken(user);

        assertEquals("alice", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, user));

        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        long ttlSeconds = (claims.getExpiration().getTime() - claims.getIssuedAt().getTime()) / 1000;
        assertTrue(ttlSeconds >= 179 && ttlSeconds <= 181);
    }
}

