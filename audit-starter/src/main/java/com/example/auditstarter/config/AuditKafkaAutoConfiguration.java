package com.example.auditstarter.config;

import com.example.auditstarter.AuditClient;
import com.example.auditstarter.impl.KafkaAuditClient;
import com.example.auditstarter.model.AuditEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Auto-wires the Kafka transport. Only activates when BOTH:
 *  - spring-kafka is on the consuming application's classpath
 *    (@ConditionalOnClass(KafkaTemplate.class))
 *  - audit.client.transport=kafka is set
 *
 * Uses a dedicated ProducerFactory/KafkaTemplate (bean name
 * "auditKafkaTemplate") with its own JSON serializer config, so this
 * doesn't collide with or depend on however the host application has
 * configured its own Kafka producers.
 *
 * Registered via META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
 */
@Configuration
@EnableConfigurationProperties(AuditClientProperties.class)
@ConditionalOnClass(KafkaTemplate.class)
@ConditionalOnProperty(prefix = "audit.client", name = "transport", havingValue = "kafka")
public class AuditKafkaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "auditKafkaProducerFactory")
    public ProducerFactory<String, AuditEvent> auditKafkaProducerFactory(Environment env) {
        Map<String, Object> configProps = new HashMap<>();
        // Reuses the standard spring.kafka.bootstrap-servers property so
        // there's only one bootstrap-servers setting to manage per service.
        String bootstrapServers = env.getProperty("spring.kafka.bootstrap-servers", "localhost:9092");
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // Favor durability over raw throughput for an audit trail.
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    @ConditionalOnMissingBean(name = "auditKafkaTemplate")
    public KafkaTemplate<String, AuditEvent> auditKafkaTemplate(
            ProducerFactory<String, AuditEvent> auditKafkaProducerFactory) {
        return new KafkaTemplate<>(auditKafkaProducerFactory);
    }

    @Bean
    @ConditionalOnMissingBean(AuditClient.class)
    public AuditClient auditClient(
            KafkaTemplate<String, AuditEvent> auditKafkaTemplate,
            AuditClientProperties properties) {
        return new KafkaAuditClient(auditKafkaTemplate, properties);
    }
}
