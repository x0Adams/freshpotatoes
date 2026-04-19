package hu.pogany.freshPotato.service;

import hu.pogany.freshPotato.security.RefreshTokenProvider;
import hu.pogany.freshPotato.config.TokenConfig;
import hu.pogany.freshPotato.entity.RefreshToken;
import hu.pogany.freshPotato.entity.User;
import hu.pogany.freshPotato.repository.RefreshTokenRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.codec.EncodingException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.AuthenticationException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

@Service
@Transactional
public class RefreshTokenService implements RefreshTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);
    private final SecureRandom random;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenConfig tokenConfig;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, TokenConfig tokenConfig) {
        random = new SecureRandom();
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenConfig = tokenConfig;
    }

    @Override
    public String getBase64Token(byte[] token) {
        return base64(token);
    }

    @Override
    public String hashToken(String token) {
        try {
            var encoder = MessageDigest.getInstance("SHA-256");
            return base64(encoder.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new EncodingException("algorithm is not supported");
        }

    }

    @Override
    public boolean isHashValid(String token, String hash) {
        return hashToken(token).equals(hash);
    }

    public void useToken(String fullToken) throws AuthenticationException {
        int id = getTokenId(fullToken);
        String tokenValue = getTokenValue(fullToken);

        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findNotUsedByIdWithLock(id);

        if (tokenOpt.isEmpty() || !isHashValid(tokenValue, tokenOpt.get().getToken()))
            throw new AuthenticationException("Invalid token");

        tokenOpt.get().setUsed(true);
    }

    @Override
    public String issueToken(User user) {
        String token = getBase64Token(getToken());
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpirationDate(Instant.now().plusMillis(tokenConfig.refreshTokenTtl().toMillis()));
        refreshToken.setToken(hashToken(token));
        refreshToken.setUsed(false);

        refreshToken = refreshTokenRepository.save(refreshToken);

        return String.format("%d.%s", refreshToken.getId(), token);
    }

    public RefreshToken getToken(String token) throws AuthenticationException {
        int id = getTokenId(token);

        return refreshTokenRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("token doesn't exist"));
    }

    private int getTokenId(String fullToken) throws AuthenticationException {
        try {
            return Integer.parseInt(fullToken.substring(0, fullToken.indexOf(".")));
        } catch (Exception e) {
            log.error("token is malformed: {}", fullToken);
            throw new AuthenticationException("malformed token");
        }
    }

    private String getTokenValue(String fullToken) throws AuthenticationException {
        try {
            return fullToken.substring(fullToken.indexOf(".") + 1);
        } catch (Exception e) {
            log.error("token is malformed: {}", fullToken);
            throw new AuthenticationException("malformed token");
        }

    }

    private String base64(byte[] str) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(str);
    }

    public byte[] getToken() {
        byte[] token = new byte[64];
        random.nextBytes(token);
        return token;
    }
}
