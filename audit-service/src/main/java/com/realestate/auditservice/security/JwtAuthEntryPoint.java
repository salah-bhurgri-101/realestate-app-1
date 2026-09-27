package com.realestate.auditservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper; // ← inject, not new

    @Override
    public void commence(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {

        Exception jwtException = (Exception) request.getAttribute("jwt_exception");

        String message;
        String title;

        if (jwtException instanceof ExpiredJwtException) {
            message = "JWT token has expired, please login again";
            title = "Token Expired";
        } else if (jwtException instanceof MalformedJwtException) {
            message = "JWT token is malformed";
            title = "Invalid Token";
        } else if (jwtException instanceof SignatureException) {
            message = "JWT signature is invalid";
            title = "Invalid Token Signature";
        } else if (jwtException instanceof UnsupportedJwtException) {
            message = "JWT token type is unsupported";
            title = "Unsupported Token";
        } else if (jwtException instanceof IllegalArgumentException) {
            message = "JWT token is missing or empty";
            title = "Missing Token";
        } else {
            message = "Authentication is required to access this resource";
            title = "Unauthorized";
        }

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, message);
        problemDetail.setTitle(title);
        problemDetail.setProperty("error_category", "Authentication");
        problemDetail.setProperty("service", "auth-service");
        problemDetail.setProperty("timestamp", Instant.now());

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(response.getWriter(), problemDetail);
    }
}
