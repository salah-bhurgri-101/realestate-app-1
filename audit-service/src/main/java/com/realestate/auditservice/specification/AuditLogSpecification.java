package com.realestate.auditservice.specification;

import com.realestate.auditservice.entity.AuditAction;
import com.realestate.auditservice.entity.AuditLog;
import java.time.Instant;
import org.springframework.data.jpa.domain.Specification;

public class AuditLogSpecification {

    private AuditLogSpecification() {}

    public static Specification<AuditLog> build(
            String serviceName,
            AuditAction action,
            String username,
            String entityType,
            String entityId,
            Instant from,
            Instant to) {

        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (serviceName != null) {
                predicates = cb.and(predicates, cb.equal(root.get("serviceName"), serviceName));
            }
            if (action != null) {
                predicates = cb.and(predicates, cb.equal(root.get("action"), action));
            }
            if (username != null) {
                predicates = cb.and(predicates, cb.equal(root.get("username"), username));
            }
            if (entityType != null) {
                predicates = cb.and(predicates, cb.equal(root.get("entityType"), entityType));
            }
            if (entityId != null) {
                predicates = cb.and(predicates, cb.equal(root.get("entityId"), entityId));
            }
            if (from != null) {
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            }
            if (to != null) {
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("createdAt"), to));
            }

            return predicates;
        };
    }
}
