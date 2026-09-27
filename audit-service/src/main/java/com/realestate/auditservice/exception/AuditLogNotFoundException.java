package com.realestate.auditservice.exception;

public class AuditLogNotFoundException extends RuntimeException {
    public AuditLogNotFoundException(String id) {
        super("Audit log not found with id: " + id);
    }
}
