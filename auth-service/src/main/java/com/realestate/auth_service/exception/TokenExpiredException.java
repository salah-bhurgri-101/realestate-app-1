package com.realestate.auth_service.exception;

public class TokenExpiredException extends AuthException {
    public TokenExpiredException(String msg) {
        super(msg);
    }
}
