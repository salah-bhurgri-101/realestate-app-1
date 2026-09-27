package com.realestate.auth_service.exception;

public class OtpExpiredException extends AuthException {
    public OtpExpiredException(String msg) {
        super(msg);
    }
}
