package hu.pogany.freshPotato.service;

import hu.pogany.freshPotato.config.TokenConfig;
import hu.pogany.freshPotato.entity.User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class JwtGeneratorService {
    NimbusJwtEncoder encoder;
    TokenConfig config;
    AuthorityService authorityService;

    public JwtGeneratorService(NimbusJwtEncoder encoder, TokenConfig config, AuthorityService authorityService) {
        this.encoder = encoder;
        this.config = config;
        this.authorityService = authorityService;
    }

    String issueToken(User user) {
        Instant issued = Instant.now();
        Instant expires = issued.plusNanos(config.refreshTokenTtl().toNanos());

        List<String> authorities = authorityService.findAuthorityByUser(user.getUsername());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(issued)
                .expiresAt(expires)
                .subject(user.getUsername())
                .claim("authorities", authorities)
                .claim("gender", user.getGender().getName())
                .claim("email", user.getEmail())
                .claim("age", user.getAge())
                .claim("uid", user.getId())
                .build();

        Jwt jwt = encoder.encode(JwtEncoderParameters.from(claims));

        return jwt.getTokenValue();
    }
}
