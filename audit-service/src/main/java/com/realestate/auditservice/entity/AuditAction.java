package com.realestate.auditservice.entity;

/**
 * Standard set of audit actions. Extend as needed; keep values
 * generic so any microservice can reuse them.
 */
public enum AuditAction {
    CREATE,
    READ,
    UPDATE,
    DELETE,
    LOGIN,
    LOGOUT,
    LOGIN_FAILED,
    ACCESS_DENIED,
    EXPORT,
    IMPORT,
    OTHER
}
