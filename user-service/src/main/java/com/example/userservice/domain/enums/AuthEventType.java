package com.example.userservice.domain.enums;

/**
 * Event types published by auth_service and consumed by this service to
 * stay eventually consistent, since there is no DB-level FK across services.
 */
public enum AuthEventType {
    USER_REGISTERED,
    USER_UPDATED,
    USER_DEACTIVATED,
    USER_REACTIVATED,
    USER_DELETED
}
