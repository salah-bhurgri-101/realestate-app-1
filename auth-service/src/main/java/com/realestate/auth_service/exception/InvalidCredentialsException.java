package com.realestate.auth_service.exception;

public class InvalidCredentialsException extends AuthException {
    public InvalidCredentialsException(String msg) {
        super(msg);
    }
}
