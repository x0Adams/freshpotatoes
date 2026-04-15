package hu.pogany.freshPotato.service;

import hu.pogany.freshPotato.dto.RegisterUserDto;
import hu.pogany.freshPotato.dto.response.TokensDto;
import hu.pogany.freshPotato.entity.Gender;
import hu.pogany.freshPotato.entity.RefreshToken;
import hu.pogany.freshPotato.entity.User;
import hu.pogany.freshPotato.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.naming.AuthenticationException;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private JwtGeneratorService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private GenderService genderService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void validateUserCredentials_shouldReturnFalse_whenUserCannotBeFoundByUsername() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        boolean result = authenticationService.validateUserCredentials("missing", "pw");

        assertFalse(result);
    }

    @Test
    void validateUserCredentials_shouldReturnFalse_whenUserExistsButIsDisabled() {
        User user = createUser("alice", "encoded", false);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        boolean result = authenticationService.validateUserCredentials("alice", "pw");

        assertFalse(result);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void validateUserCredentials_shouldReturnFalse_whenPasswordDoesNotMatchEncodedPassword() {
        User user = createUser("alice", "encoded", true);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("bad-password", "encoded")).thenReturn(false);

        boolean result = authenticationService.validateUserCredentials("alice", "bad-password");

        assertFalse(result);
    }

    @Test
    void validateUserCredentials_shouldReturnTrue_whenUserExistsIsEnabledAndPasswordMatches() {
        User user = createUser("alice", "encoded", true);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct-password", "encoded")).thenReturn(true);

        boolean result = authenticationService.validateUserCredentials("alice", "correct-password");

        assertTrue(result);
    }

    @Test
    void login_shouldThrowAuthenticationException_whenProvidedCredentialsAreInvalid() {
        when(userRepository.findByUsername("unknown-user")).thenReturn(Optional.empty());

        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> authenticationService.login("unknown-user", "pw")
        );

        assertEquals("Invalid user credentials", exception.getMessage());
    }

    @Test
    void login_shouldReturnJwtAndRefreshTokens_whenProvidedCredentialsAreValid() throws AuthenticationException {
        User user = createUser("alice", "encoded", true);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct-password", "encoded")).thenReturn(true);
        when(jwtService.issueToken(user)).thenReturn("jwt-value");
        when(refreshTokenService.issueToken(user)).thenReturn("refresh-value");

        TokensDto result = authenticationService.login("alice", "correct-password");

        assertEquals("jwt-value", result.jwtToken());
        assertEquals("refresh-value", result.refreshToken());
    }

    @Test
    void refresh_shouldThrowAuthenticationException_whenRefreshTokenHasExpiredEvenIfTokenWasUnused() throws AuthenticationException {
        String refreshToken = "4.raw";
        RefreshToken refreshTokenEntity = new RefreshToken();
        refreshTokenEntity.setExpirationDate(Instant.now().minusSeconds(1));

        doNothing().when(refreshTokenService).useToken(refreshToken);
        when(refreshTokenService.getToken(refreshToken)).thenReturn(refreshTokenEntity);

        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> authenticationService.refresh(refreshToken)
        );

        assertEquals("Expired token", exception.getMessage());
        verify(jwtService, never()).issueToken(any());
    }

    @Test
    void refresh_shouldReturnNewTokens_whenRefreshTokenIsValidAndNotExpired() throws AuthenticationException {
        String refreshToken = "7.raw";
        User issuer = createUser("issuer", "encoded", true);
        RefreshToken refreshTokenEntity = new RefreshToken();
        refreshTokenEntity.setUser(issuer);
        refreshTokenEntity.setExpirationDate(Instant.now().plusSeconds(300));

        doNothing().when(refreshTokenService).useToken(refreshToken);
        when(refreshTokenService.getToken(refreshToken)).thenReturn(refreshTokenEntity);
        when(jwtService.issueToken(issuer)).thenReturn("new-jwt");
        when(refreshTokenService.issueToken(issuer)).thenReturn("new-refresh");

        TokensDto result = authenticationService.refresh(refreshToken);

        assertEquals("new-jwt", result.jwtToken());
        assertEquals("new-refresh", result.refreshToken());
        verify(refreshTokenService, times(2)).getToken(refreshToken);
    }

    @Test
    void logout_shouldDelegateRefreshTokenConsumptionToRefreshTokenService() throws AuthenticationException {
        authenticationService.logout("10.raw-token");

        verify(refreshTokenService).useToken("10.raw-token");
    }

    @Test
    void register_shouldBuildEnabledUserWithEncodedPasswordAndDelegatePersistingToUserService() {
        RegisterUserDto dto = new RegisterUserDto("mail@test.dev", "new-user", "male", 25, "raw-password");
        Gender gender = new Gender();
        gender.setName("male");
        when(genderService.findByName("male")).thenReturn(gender);
        when(passwordEncoder.encode("raw-password")).thenReturn("encoded-password");

        authenticationService.register(dto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userService).save(captor.capture());
        User savedUser = captor.getValue();

        assertEquals("mail@test.dev", savedUser.getEmail());
        assertEquals("new-user", savedUser.getUsername());
        assertEquals(gender, savedUser.getGender());
        assertEquals(25, savedUser.getAge());
        assertEquals("encoded-password", savedUser.getPassword());
        assertTrue(savedUser.getEnabled());
    }

    @Test
    void registerAdmin_shouldBuildEnabledUserWithEncodedPasswordAndDelegatePersistingAsAdminToUserService() {
        RegisterUserDto dto = new RegisterUserDto("admin@test.dev", "new-admin", "female", 31, "raw-password");
        Gender gender = new Gender();
        gender.setName("female");
        when(genderService.findByName("female")).thenReturn(gender);
        when(passwordEncoder.encode("raw-password")).thenReturn("encoded-password");

        authenticationService.registerAdmin(dto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userService).saveAsAdmin(captor.capture());
        User savedUser = captor.getValue();

        assertEquals("admin@test.dev", savedUser.getEmail());
        assertEquals("new-admin", savedUser.getUsername());
        assertEquals(gender, savedUser.getGender());
        assertEquals(31, savedUser.getAge());
        assertEquals("encoded-password", savedUser.getPassword());
        assertTrue(savedUser.getEnabled());
    }

    private User createUser(String username, String password, boolean enabled) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEnabled(enabled);
        return user;
    }
}

