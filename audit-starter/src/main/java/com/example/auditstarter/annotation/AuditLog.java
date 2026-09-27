package com.example.auditstarter.annotation;

import com.example.auditstarter.model.AuditAction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Put this on any service/controller method in YOUR microservice to have
 * it automatically reported to the centralized audit-service via
 * AuditClient — no manual logging calls needed.
 *
 * Example:
 * <pre>
 *   {@literal @}AuditLog(action = AuditAction.UPDATE, entityType = "Order", entityIdExpression = "#orderId")
 *   public Order updateOrder(String orderId, OrderRequest req) { ... }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {

    AuditAction action();

    String entityType() default "";

    /** SpEL expression evaluated against method parameter names, e.g. "#orderId" or "#request.id". */
    String entityIdExpression() default "";

    String description() default "";
}
