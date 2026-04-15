package hu.pogany.freshPotato.service;

import hu.pogany.freshPotato.config.TokenConfig;
import hu.pogany.freshPotato.entity.RefreshToken;
import hu.pogany.freshPotato.entity.User;
import hu.pogany.freshPotato.repository.RefreshTokenRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.naming.AuthenticationException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private TokenConfig tokenConfig;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Test
    void hashToken_shouldReturnSameHashForSameInputAndDifferentHashForDifferentInput_whenUsingSha256() {
        String firstHash = refreshTokenService.hashToken("abc123");
        String sameHash = refreshTokenService.hashToken("abc123");
        String differentHash = refreshTokenService.hashToken("different");

        assertEquals(firstHash, sameHash);
        assertNotEquals(firstHash, differentHash);
        assertFalse(firstHash.contains("="));
    }

    @Test
    void isHashValid_shouldReturnTrueOnlyWhenGivenTokenMatchesGivenHash() {
        String token = "raw-token-value";
        String validHash = refreshTokenService.hashToken(token);

        assertTrue(refreshTokenService.isHashValid(token, validHash));
        assertFalse(refreshTokenService.isHashValid("other-token", validHash));
    }

    @Test
    void getBase64Token_shouldReturnUrlSafeTokenWithoutPadding_forBinaryInput() {
        String encoded = refreshTokenService.getBase64Token(new byte[]{1, 2, 3});

        assertEquals("AQID", encoded);
    }

    @Test
    void issueToken_shouldPersistUnusedHashedTokenWithConfiguredExpirationAndReturnIdDotRawToken() {
        User user = createUser(12);
        Duration refreshTtl = Duration.ofMinutes(10);
        when(tokenConfig.refreshTokenTtl()).thenReturn(refreshTtl);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> {
            RefreshToken toSave = invocation.getArgument(0);
            toSave.setId(42);
            return toSave;
        });

        Instant beforeIssue = Instant.now();
        String fullToken = refreshTokenService.issueToken(user);
        Instant afterIssue = Instant.now();

        String[] parts = fullToken.split("\\.", 2);
        assertEquals("42", parts[0]);
        assertEquals(2, parts.length);

        String rawToken = parts[1];
        assertEquals(64, Base64.getUrlDecoder().decode(rawToken).length);

        ArgumentCaptor<RefreshToken> savedTokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        org.mockito.Mockito.verify(refreshTokenRepository).save(savedTokenCaptor.capture());
        RefreshToken persisted = savedTokenCaptor.getValue();

        assertEquals(user, persisted.getUser());
        assertFalse(persisted.getUsed());
        assertEquals(refreshTokenService.hashToken(rawToken), persisted.getToken());

        Instant minExpectedExpiration = beforeIssue.plus(refreshTtl);
        Instant maxExpectedExpiration = afterIssue.plus(refreshTtl);
        assertFalse(persisted.getExpirationDate().isBefore(minExpectedExpiration));
        assertFalse(persisted.getExpirationDate().isAfter(maxExpectedExpiration));
    }

    @Test
    void useToken_shouldMarkTokenAsUsed_whenTokenIdExistsAndHashMatchesAndTokenIsUnused() throws AuthenticationException {
        RefreshToken tokenEntity = new RefreshToken();
        tokenEntity.setId(7);
        tokenEntity.setToken(refreshTokenService.hashToken("plain-token"));
        tokenEntity.setUsed(false);

        when(refreshTokenRepository.findNotUsedByIdWithLock(7)).thenReturn(Optional.of(tokenEntity));

        refreshTokenService.useToken("7.plain-token");

        assertTrue(tokenEntity.getUsed());
    }

    @Test
    void useToken_shouldThrowAuthenticationException_whenTokenFormatIsMalformed() {
        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> refreshTokenService.useToken("malformed-token-without-dot")
        );

        assertEquals("malformed token", exception.getMessage());
    }

    @Test
    void useToken_shouldThrowAuthenticationException_whenTokenIdDoesNotExistOrWasAlreadyUsed() {
        when(refreshTokenRepository.findNotUsedByIdWithLock(99)).thenReturn(Optional.empty());

        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> refreshTokenService.useToken("99.anything")
        );

        assertEquals("Invalid token", exception.getMessage());
    }

    @Test
    void useToken_shouldThrowAuthenticationException_whenTokenHashDoesNotMatchStoredHash() {
        RefreshToken tokenEntity = new RefreshToken();
        tokenEntity.setId(5);
        tokenEntity.setToken(refreshTokenService.hashToken("expected"));
        tokenEntity.setUsed(false);

        when(refreshTokenRepository.findNotUsedByIdWithLock(5)).thenReturn(Optional.of(tokenEntity));

        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> refreshTokenService.useToken("5.unexpected")
        );

        assertEquals("Invalid token", exception.getMessage());
    }

    @Test
    void getToken_shouldReturnEntity_whenTokenIdCanBeExtractedFromInput() throws AuthenticationException {
        RefreshToken tokenEntity = new RefreshToken();
        tokenEntity.setId(13);
        when(refreshTokenRepository.findById(13)).thenReturn(Optional.of(tokenEntity));

        RefreshToken result = refreshTokenService.getToken("13.token-value");

        assertEquals(tokenEntity, result);
    }

    @Test
    void getToken_shouldThrowAuthenticationException_whenTokenIdCannotBeExtracted() {
        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> refreshTokenService.getToken("not-a-valid-token")
        );

        assertEquals("malformed token", exception.getMessage());
    }

    @Test
    void getToken_shouldThrowEntityNotFoundException_whenTokenIdIsValidButEntityIsMissing() {
        when(refreshTokenRepository.findById(100)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> refreshTokenService.getToken("100.any-value")
        );

        assertEquals("token doesn't exist", exception.getMessage());
    }

    private User createUser(int id) {
        User user = new User();
        user.setId(id);
        user.setUsername("user" + id);
        return user;
    }
}

