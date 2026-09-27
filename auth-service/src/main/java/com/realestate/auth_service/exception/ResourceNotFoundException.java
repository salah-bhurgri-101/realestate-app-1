package com.realestate.auth_service.exception;

public class ResourceNotFoundException extends AuthException {
    public ResourceNotFoundException(String msg) {
        super(msg);
    }
}
