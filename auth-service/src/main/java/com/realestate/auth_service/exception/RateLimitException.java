package com.realestate.auth_service.exception;

public class RateLimitException extends AuthException {
    public RateLimitException(String msg) {
        super(msg);
    }
}
