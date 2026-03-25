package com.myapp.learningtube.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    private final CorsProperties corsProperties;

    public CorsConfig(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOrigins(corsProperties.getAllowedOrigins());
        c.setAllowedMethods(corsProperties.getAllowedMethods());
        c.setAllowedHeaders(corsProperties.getAllowedHeaders());
        c.setExposedHeaders(corsProperties.getExposedHeaders());
        c.setAllowCredentials(corsProperties.isAllowCredentials());
        c.setMaxAge(corsProperties.getMaxAgeSeconds());
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", c);
        return source;
    }
}
