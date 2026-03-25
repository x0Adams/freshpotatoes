package hu.pogany.freshPotato.service;

import hu.pogany.freshPotato.Security.RefreshTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
public class RefreshTokenService implements RefreshTokenProvider {

    private final PasswordEncoder encoder;
    private final SecureRandom random;

    public RefreshTokenService(PasswordEncoder encoder) {
        this.encoder = encoder;
        random = new SecureRandom();
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
}
