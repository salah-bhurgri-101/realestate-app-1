package com.example.auditstarter.aspect;

import com.example.auditstarter.AuditClient;
import com.example.auditstarter.annotation.AuditLog;
import com.example.auditstarter.model.AuditEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Mirror of the aspect bundled with audit-service itself, but this one
 * reports events over the network via AuditClient instead of writing
 * directly to a local database. Drop this jar on the classpath of ANY
 * microservice and annotate methods with @AuditLog to get automatic,
 * non-blocking audit reporting.
 *
 * NOT annotated with @Component — deliberately. This class lives in
 * com.example.auditstarter, which is outside every consuming service's
 * own base package, so Spring's default component-scan would never find
 * it. The bean is registered explicitly by AuditAspectAutoConfiguration
 * instead (the correct pattern for a cross-module Spring Boot starter).
 */
@Slf4j
@Aspect
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditClient auditClient;
    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(auditable)")
    public Object around(ProceedingJoinPoint joinPoint, AuditLog auditable) throws Throwable {
        String entityId = resolveEntityId(joinPoint, auditable.entityIdExpression());
        String status = "SUCCESS";
        String failureReason = null;

        try {
            return joinPoint.proceed();
        } catch (Throwable ex) {
            status = "FAILURE";
            failureReason = ex.getMessage();
            throw ex;
        } finally {
            AuditEvent event = AuditEvent.builder()
                    .action(auditable.action())
                    .entityType(auditable.entityType())
                    .entityId(entityId)
                    .username(resolveUsername())
                    .ipAddress(resolveClientIp())
                    .description(failureReason == null
                            ? auditable.description()
                            : auditable.description() + " | error: " + failureReason)
                    .status(status)
                    .build();

            // Fire-and-forget — never blocks the method this aspect wraps.
            auditClient.log(event);
        }
    }

    private String resolveEntityId(ProceedingJoinPoint joinPoint, String expression) {
        if (expression == null || expression.isBlank()) return null;
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();

            StandardEvaluationContext context = new StandardEvaluationContext();
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
            Expression exp = parser.parseExpression(expression);
            Object value = exp.getValue(context);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            log.debug("Could not resolve entityId expression '{}': {}", expression, e.getMessage());
            return null;
        }
    }

    /**
     * Default reads the username forwarded by an upstream gateway. If your
     * service uses Spring Security, add it as a dependency in the consuming
     * application and replace this with the authenticated principal name.
     */
    private String resolveUsername() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return "system";
            String user = attrs.getRequest().getHeader("X-User-Name");
            return user != null ? user : "system";
        } catch (Exception e) {
            return "system";
        }
    }

    private String resolveClientIp() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return null;
            String forwarded = attrs.getRequest().getHeader("X-Forwarded-For");
            return (forwarded != null) ? forwarded.split(",")[0].trim() : attrs.getRequest().getRemoteAddr();
        } catch (Exception e) {
            return null;
        }
    }
}
