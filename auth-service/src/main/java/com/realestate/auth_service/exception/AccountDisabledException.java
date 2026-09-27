package com.realestate.auth_service.exception;

public class AccountDisabledException extends AuthException {
    public AccountDisabledException(String msg) {
        super(msg);
    }
}
