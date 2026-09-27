package com.realestate.auth_service.exception;

public class InvalidTokenException extends AuthException {
    public InvalidTokenException(String msg) {
        super(msg);
    }
}
