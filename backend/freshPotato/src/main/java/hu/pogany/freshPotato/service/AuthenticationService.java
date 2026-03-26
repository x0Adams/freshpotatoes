package hu.pogany.freshPotato.service;

import hu.pogany.freshPotato.dto.TokensDto;
import hu.pogany.freshPotato.entity.User;
import hu.pogany.freshPotato.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.AuthenticationException;
import java.util.Optional;

@Transactional
@Service
public class AuthenticationService {
    JwtGeneratorService jwtService;
    RefreshTokenService refreshTokenService;
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;

    public AuthenticationService(JwtGeneratorService jwtService, RefreshTokenService refreshTokenService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    boolean validateUserCredentials(String username, String password){
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty())
            return false;

        User user = userOpt.get();

        if (user.getEnabled() == false)
            return false;

        if (!user.getUsername().equals(username))
            return false;

        String passHash = passwordEncoder.encode(password);
        return user.getPassword().equals(passHash);

    }

    public TokensDto login(String username, String password) throws AuthenticationException {
        if (!validateUserCredentials(username, password))
            throw new AuthenticationException("Invalid user credentials");

        User user = userRepository.findByUsername(username).get();

        return new TokensDto(
                jwtService.issueToken(user),
                refreshTokenService.issueToken(user)
        );
    }

    public TokensDto refresh(String refreshToken) throws AuthenticationException {
        refreshTokenService.useToken(refreshToken);

        User issuer = refreshTokenService.getToken(refreshToken).getUser();

        return new TokensDto(
                jwtService.issueToken(issuer),
                refreshTokenService.issueToken(issuer)
        );
    }

    public void logout(String token) throws AuthenticationException {
        refreshTokenService.useToken(token);
    }
}
