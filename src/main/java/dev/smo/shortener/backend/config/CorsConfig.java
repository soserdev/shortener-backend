package dev.smo.shortener.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Apply to all endpoints
                        .allowedOrigins(allowedOrigins.toArray(new String[0])) // Replace with your frontend domain
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH") // Allow these methods
                        .allowedHeaders("X-Custom-Header", "Content-Type", "Authorization") // Allow specific headers
                        .allowCredentials(true) // Allow cookies or other credentials
                        .maxAge(3600); // Cache preflight response for 1 hour
            }
        };
    }
}
