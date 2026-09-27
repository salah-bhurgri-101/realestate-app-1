package com.realestate.auditservice.repository;

import com.realestate.auditservice.entity.AuditAction;
import com.realestate.auditservice.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AuditLogRepository extends JpaRepository<AuditLog, String>, JpaSpecificationExecutor<AuditLog> {

    Page<AuditLog> findByServiceName(String serviceName, Pageable pageable);

    Page<AuditLog> findByUsername(String username, Pageable pageable);

    Page<AuditLog> findByEntityTypeAndEntityId(String entityType, String entityId, Pageable pageable);

    Page<AuditLog> findByAction(AuditAction action, Pageable pageable);

    //    @Query("""
    //           SELECT a FROM AuditLog a
    //           WHERE (:serviceName IS NULL OR a.serviceName = :serviceName)
    //             AND (:action IS NULL OR a.action = :action)
    //             AND (:username IS NULL OR a.username = :username)
    //             AND (:entityType IS NULL OR a.entityType = :entityType)
    //             AND (:entityId IS NULL OR a.entityId = :entityId)
    //             AND (:from IS NULL OR a.createdAt >= :from)
    //             AND (:to IS NULL OR a.createdAt <= :to)
    //           """)
    //    Page<AuditLog> search(
    //            @Param("serviceName") String serviceName,
    //            @Param("action") AuditAction action,
    //            @Param("username") String username,
    //            @Param("entityType") String entityType,
    //            @Param("entityId") String entityId,
    //            @Param("from") Instant from,
    //            @Param("to") Instant to,
    //            Pageable pageable
    //    );

    //    @Query(
    //            value =
    //                    """
    //    SELECT *
    //    FROM audit_logs a
    //    WHERE (CAST(:serviceName AS varchar) IS NULL OR a.service_name = :serviceName)
    //      AND (CAST(:action AS varchar) IS NULL OR a.action = :action)
    //      AND (CAST(:username AS varchar) IS NULL OR a.username = :username)
    //      AND (CAST(:entityType AS varchar) IS NULL OR a.entity_type = :entityType)
    //      AND (CAST(:entityId AS varchar) IS NULL OR a.entity_id = :entityId)
    //      AND (CAST(:from AS timestamptz) IS NULL OR a.created_at >= :from)
    //      AND (CAST(:to AS timestamptz) IS NULL OR a.created_at <= :to)
    //    """,
    //            countQuery =
    //                    """
    //    SELECT count(*)
    //    FROM audit_logs a
    //    WHERE (CAST(:serviceName AS varchar) IS NULL OR a.service_name = :serviceName)
    //      AND (CAST(:action AS varchar) IS NULL OR a.action = :action)
    //      AND (CAST(:username AS varchar) IS NULL OR a.username = :username)
    //      AND (CAST(:entityType AS varchar) IS NULL OR a.entity_type = :entityType)
    //      AND (CAST(:entityId AS varchar) IS NULL OR a.entity_id = :entityId)
    //      AND (CAST(:from AS timestamptz) IS NULL OR a.created_at >= :from)
    //      AND (CAST(:to AS timestamptz) IS NULL OR a.created_at <= :to)
    //    """,
    //            nativeQuery = true)
    //    Page<AuditLog> search(
    //            @Param("serviceName") String serviceName,
    //            @Param("action") String action,
    //            @Param("username") String username,
    //            @Param("entityType") String entityType,
    //            @Param("entityId") String entityId,
    //            @Param("from") Instant from,
    //            @Param("to") Instant to,
    //            Pageable pageable);
}
