package com.realestate.auth_service.security;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;

    @Value("${auth.cors.allowed-origins:http://localhost:3000}")
    private String allowedOrigins; // use in one corsConfigurationSource()

    private static final String[] PUBLIC_ROUTES = {
        "/api/v1/auth/register",
        "/api/v1/auth/otp/verify",
        "/api/v1/auth/otp/resend",
        "/api/v1/auth/login",
        "/api/v1/auth/token/refresh",
        "/api/v1/auth/password/forgot",
        "/api/v1/auth/password/reset",
        // Swagger / Actuator (remove in production if not needed)
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/actuator/health"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(
                        ex -> ex.authenticationEntryPoint(jwtAuthEntryPoint) // ← register karo
                        )
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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    //    @Bean
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

        config.setAllowedOriginPatterns(List.of(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("*"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
