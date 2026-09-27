package com.realestate.auth_service.exception;

public class AccountNotVerifiedException extends AuthException {
    public AccountNotVerifiedException(String msg) {
        super(msg);
    }
}
