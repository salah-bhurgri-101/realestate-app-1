package com.realestate.auth_service.exception;

public class UserNotFoundException extends AuthException {
    public UserNotFoundException(String msg) {
        super(msg);
    }
}
