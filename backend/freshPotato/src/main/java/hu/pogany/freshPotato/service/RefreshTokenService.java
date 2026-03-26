package hu.pogany.freshPotato.service;

import hu.pogany.freshPotato.Security.RefreshTokenProvider;
import hu.pogany.freshPotato.config.TokenConfig;
import hu.pogany.freshPotato.entity.RefreshToken;
import hu.pogany.freshPotato.entity.User;
import hu.pogany.freshPotato.repository.RefreshTokenRepository;
import org.antlr.v4.runtime.Token;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.AuthenticationException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
@Transactional
public class RefreshTokenService implements RefreshTokenProvider {

    private final PasswordEncoder encoder;
    private final SecureRandom random;
    RefreshTokenRepository refreshTokenRepository;
    TokenConfig tokenConfig;

    public RefreshTokenService(PasswordEncoder encoder, RefreshTokenRepository refreshTokenRepository, TokenConfig tokenConfig) {
        this.encoder = encoder;
        random = new SecureRandom();
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenConfig = tokenConfig;
    }

    @Override
    public String getBase64Token() {
        byte[] token = new byte[64];
        random.nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
    }

    @Override
    public String hashToken(String token) {
        return encoder.encode(token);
    }

    @Override
    public boolean isHashValid(String token, String hash) {
        return encoder.matches(token, hash);
    }

    public void useToken(int userId, String token) throws AuthenticationException {
        String hash = hashToken(token);

        if (refreshTokenRepository.updateUsedToFalse(userId, hash) == 0)
            throw new AuthenticationException("token doesn't exist or it was used");
    }

    public String generateTokenForUser(User user) {
        String base64Token = getBase64Token();

        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setExpirationDate(Instant.now().plusMillis(tokenConfig.refreshTokenTtl().toMillis()));
        token.setToken(hashToken(base64Token));
        token.setUsed(0);

        refreshTokenRepository.save(token);

        return base64Token;
    }
}
