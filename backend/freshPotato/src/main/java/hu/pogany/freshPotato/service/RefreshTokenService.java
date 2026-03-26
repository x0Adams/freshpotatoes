package hu.pogany.freshPotato.service;

import hu.pogany.freshPotato.Security.RefreshTokenProvider;
import hu.pogany.freshPotato.config.TokenConfig;
import hu.pogany.freshPotato.entity.RefreshToken;
import hu.pogany.freshPotato.entity.User;
import hu.pogany.freshPotato.repository.RefreshTokenRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.AuthenticationException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

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

    public void useToken(String fullToken) throws AuthenticationException {
        int id = getTokenId(fullToken);
        String tokenValue = getTokenValue(fullToken);

        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByIdWithLock(id);

        if (tokenOpt.isEmpty() || !isHashValid(tokenValue, tokenOpt.get().getToken()))
            throw new AuthenticationException("Invalid token");

        tokenOpt.get().setUsed(true);
    }

    @Override
    public String issueToken(User user) {
        String base64Token = getBase64Token();

        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setExpirationDate(Instant.now().plusMillis(tokenConfig.refreshTokenTtl().toMillis()));
        token.setToken(hashToken(base64Token));
        token.setUsed(false);

        token = refreshTokenRepository.save(token);

        return String.format("%d.%s", token.getId(), base64Token);
    }

    public RefreshToken getToken(String token) {
        int id = getTokenId(token);

        return refreshTokenRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("token doesn't exist"));
    }

    private int getTokenId(String fullToken) {
        return Integer.parseInt(fullToken.substring(0, fullToken.indexOf(".")));
    }

    private String getTokenValue(String fullToken) {
        return fullToken.substring(fullToken.indexOf(".") + 1);
    }
}
