package hu.pogany.freshPotato.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import(SecurityConfig.class)
@Configuration
public class Config {

}
