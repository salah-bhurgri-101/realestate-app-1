package com.realestate.auth_service.exception;

public class OtpMaxAttemptsException extends AuthException {
    public OtpMaxAttemptsException(String msg) {
        super(msg);
    }
}
