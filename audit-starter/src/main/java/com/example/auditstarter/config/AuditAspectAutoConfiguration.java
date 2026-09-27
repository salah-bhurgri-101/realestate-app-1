package com.example.auditstarter.config;

import com.example.auditstarter.AuditClient;
import com.example.auditstarter.aspect.AuditAspect;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Registers AuditAspect as a bean so @AuditLog actually does something.
 *
 * This is THE fix for "@AuditLog annotation lagaya par kuch ho hi nahi
 * raha": AuditAspect is a plain @Aspect class with no @Component, because
 * com.example.auditstarter is never inside a consuming service's own
 * component-scan base package (e.g. com.example.orderservice). Spring
 * Boot starters must register their beans explicitly via
 * auto-configuration — they cannot rely on the host app's component scan
 * reaching into the starter's packages. This config does exactly that.
 *
 * @ConditionalOnBean(AuditClient.class) ensures the aspect only activates
 * once a transport (WebClient or Kafka) is actually wired up — see
 * AuditWebClientAutoConfiguration / AuditKafkaAutoConfiguration.
 *
 * @EnableAspectJAutoProxy(proxyTargetClass = true) makes sure CGLIB
 * proxying is on even if the consuming app hasn't enabled it itself —
 * spring-boot-starter-aop alone does NOT guarantee this for every setup.
 */
@Configuration
@AutoConfigureAfter({AuditWebClientAutoConfiguration.class, AuditKafkaAutoConfiguration.class})
@EnableAspectJAutoProxy(proxyTargetClass = true)
@ConditionalOnProperty(prefix = "audit.client", name = "annotation-enabled", havingValue = "true", matchIfMissing = true)
public class AuditAspectAutoConfiguration {

    @Bean
    @ConditionalOnBean(AuditClient.class)
    @ConditionalOnMissingBean(AuditAspect.class)
    public AuditAspect auditAspect(AuditClient auditClient) {
        return new AuditAspect(auditClient);
    }
}
