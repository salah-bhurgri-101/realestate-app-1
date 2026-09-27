package com.example.auditstarter.model;

/**
 * Mirrors com.example.auditservice.entity.AuditAction.
 * Kept as a separate copy so this client has zero compile dependency
 * on the audit-service module.
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
