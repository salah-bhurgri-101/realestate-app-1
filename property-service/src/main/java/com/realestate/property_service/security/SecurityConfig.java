package com.realestate.property_service.security;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    //    @Value("${auth.cors.allowed-origins:http://localhost:3000}")
    //    private String allowedOrigins; // use in one corsConfigurationSource()

    private static final String[] PUBLIC_ROUTES = {
        "/api/properties/search",
        "/api/properties/featured",
        //            "/api/properties/*", //commit it beacuse No Check security to Hit api.

        //        "/api/**",
        //            "/api/properties/**", //commit it beacuse No Check security to Hit api.
        //            /* and /** big Different Hai /* and /** , /api/properties/** and /api/properties/*
        //           /* me yeh api work kare ga like /api/properties/id ,/api/properties/search
        //            /*  me yha nhi api work kare ge /api/properties and /api/properties/id/staus
        // ,/api/properties/ok/search etc
        //            /**  all working /api/properties , /api/properties/id ,/api/properties/id/status

        // Swagger / Actuator (remove in production if not needed)
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/actuator/health"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                //                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth.requestMatchers(PUBLIC_ROUTES)
                        .permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    //   @Bean
    //    public CorsConfigurationSource corsConfigurationSource() {
    //        CorsConfiguration config = new CorsConfiguration();
    //        config.setAllowedOrigins(List.of(allowedOrigins.split(",")));
    //        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    //        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Forwarded-For"));
    //        config.setExposedHeaders(List.of("Authorization"));
    //        config.setAllowCredentials(true);
    //        config.setMaxAge(3600L);
    //
    //        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    //        source.registerCorsConfiguration("/**", config);
    //        return source;
    //    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOriginPatterns(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("*"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
