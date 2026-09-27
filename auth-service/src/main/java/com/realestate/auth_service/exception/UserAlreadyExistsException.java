package com.realestate.auth_service.exception;

public class UserAlreadyExistsException extends AuthException {
    public UserAlreadyExistsException(String msg) {
        super(msg);
    }
}
