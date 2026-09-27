package com.realestate.auth_service.exception;

public class OtpNotFoundException extends AuthException {
    public OtpNotFoundException(String msg) {
        super(msg);
    }
}
