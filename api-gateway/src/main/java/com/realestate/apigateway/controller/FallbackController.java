package com.realestate.apigateway.controller;

import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FallbackController {

    @GetMapping("/fallback/auth")
    public ResponseEntity<Map<String, Object>> authFallback() {
        return unavailable("auth_service_unavailable", "Authentication service is temporarily unavailable.");
    }

    @GetMapping("/fallback/property")
    public ResponseEntity<Map<String, Object>> propertyFallback() {
        return unavailable("property_service_unavailable", "Property service is temporarily unavailable.");
    }

    @GetMapping("/fallback/audit")
    public ResponseEntity<Map<String, Object>> auditFallback() {
        return unavailable("audit_service_unavailable", "Audit service is temporarily unavailable.");
    }

    @GetMapping("/fallback/user")
    public ResponseEntity<Map<String, Object>> userFallback() {
        return unavailable("user_service_unavailable", "User service is temporarily unavailable.");
    }

    private ResponseEntity<Map<String, Object>> unavailable(String code, String message) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", code,
                        "message", message,
                        "timestamp", Instant.now().toString()));
    }
}
