package com.ages.pie.application.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Configuração técnica de CORS. Não deve conter nenhuma lógica de negócio.
 *
 * Portas 8081 (Metro Bundler) e 19006 (Expo dev server) liberadas para o
 * front em React Native. Se o time migrar de Expo para RN CLI puro, a
 * 19006 deixa de ser necessária, mas mantê-la não quebra nada.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);

        config.setAllowedOrigins(Arrays.asList(
            "http://localhost:8081",
            "http://localhost:19006",
            "http://127.0.0.1:8081",
            "http://127.0.0.1:19006"
        ));

        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setExposedHeaders(Arrays.asList("Authorization"));

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}