package hu.pogany.freshPotato.service;

import hu.pogany.freshPotato.dto.RegisterUserDto;
import hu.pogany.freshPotato.dto.response.TokensDto;
import hu.pogany.freshPotato.entity.Gender;
import hu.pogany.freshPotato.entity.RefreshToken;
import hu.pogany.freshPotato.entity.User;
import hu.pogany.freshPotato.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.AuthenticationException;
import java.time.Instant;
import java.util.Optional;

@Transactional
@Service
public class AuthenticationService {
    private final JwtGeneratorService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final GenderService genderService;
    private final UserService userService;

    public AuthenticationService(JwtGeneratorService jwtService, RefreshTokenService refreshTokenService, UserRepository userRepository, PasswordEncoder passwordEncoder, GenderService genderService, UserService userService) {
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.genderService = genderService;
        this.userService = userService;
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

        return passwordEncoder.matches(password, user.getPassword());

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

        RefreshToken tokenObj = refreshTokenService.getToken(refreshToken);
        if (tokenObj.getExpirationDate().isBefore(Instant.now()))
            throw new AuthenticationException("Expired token");

        User issuer = refreshTokenService.getToken(refreshToken).getUser();

        return new TokensDto(
                jwtService.issueToken(issuer),
                refreshTokenService.issueToken(issuer)
        );
    }

    public void logout(String token) throws AuthenticationException {
        refreshTokenService.useToken(token);
    }

    public void register(RegisterUserDto registerUserDto) {
        Gender gender = genderService.findByName(registerUserDto.genderName());
        User newUser = fromRegisterUserDto(registerUserDto, gender);

        userService.save(newUser);
    }

    public void registerAdmin(RegisterUserDto registerUserDto) {
        Gender gender = genderService.findByName(registerUserDto.genderName());
        User newUser = fromRegisterUserDto(registerUserDto, gender);

        userService.saveAsAdmin(newUser);
    }



    private User fromRegisterUserDto(RegisterUserDto registerer, Gender gender) {
        return User.builder()
                .username(registerer.username())
                .email(registerer.email())
                .gender(gender)
                .age(registerer.age())
                .password(passwordEncoder.encode(registerer.password()))
                .enabled(true)
                .build();
    }
}
