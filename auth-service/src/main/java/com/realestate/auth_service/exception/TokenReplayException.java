package com.realestate.auth_service.exception;

public class TokenReplayException extends AuthException {
    public TokenReplayException(String msg) {
        super(msg);
    }
}
