package hu.pogany.freshPotato.controller;

import hu.pogany.freshPotato.dto.LoginDto;
import hu.pogany.freshPotato.dto.RefreshTokenDto;
import hu.pogany.freshPotato.dto.RegisterUserDto;
import hu.pogany.freshPotato.dto.TokensDto;
import hu.pogany.freshPotato.service.AuthenticationService;
import jakarta.persistence.EntityExistsException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationService authService;

    public AuthController(AuthenticationService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public TokensDto login(@RequestBody LoginDto loginData) throws AuthenticationException {
        TokensDto tokens = authService.login(loginData.username(), loginData.password());
        return tokens;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody RegisterUserDto userData) {
        try {
            authService.register(userData);
        } catch (EntityExistsException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.ok().body("User is created");
    }

    @PostMapping("/admin/register")
    public ResponseEntity<String> registerAdmin(@RequestBody RegisterUserDto userData) {
        try {
            authService.registerAdmin(userData);
        } catch (EntityExistsException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.ok().body("Admin is created");
    }

    @PostMapping("/refresh")
    public TokensDto refresh(@RequestBody RefreshTokenDto refreshToken) throws AuthenticationException {
        return authService.refresh(refreshToken.refreshToken());
    }

    @PostMapping("/logout")
    public void logout(@RequestBody RefreshTokenDto refreshToken) throws AuthenticationException {
        authService.logout(refreshToken.refreshToken());
    }

    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
        return jwt.getClaims();
    }


}
