package hu.pogany.freshPotato.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Import(SecurityConfig.class)
@Configuration
public class Config {
    
}
