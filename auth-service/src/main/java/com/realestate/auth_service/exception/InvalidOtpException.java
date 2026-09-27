package com.realestate.auth_service.exception;

public class InvalidOtpException extends AuthException {
    public InvalidOtpException(String msg) {
        super(msg);
    }
}
