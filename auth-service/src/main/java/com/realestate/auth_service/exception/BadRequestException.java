package com.realestate.auth_service.exception;

public class BadRequestException extends AuthException {
    public BadRequestException(String msg) {
        super(msg);
    }
}
