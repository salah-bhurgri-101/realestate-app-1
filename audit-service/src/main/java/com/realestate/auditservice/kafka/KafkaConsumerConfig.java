package com.realestate.auditservice.kafka;

import com.realestate.auditservice.dto.AuditLogRequest;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.*;
import org.springframework.util.backoff.ExponentialBackOff;

@Slf4j
@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${audit.kafka.consumer-group:audit-service}")
    private String groupId;

    @Value("${audit.kafka.topic:audit-events}")
    private String auditTopic;

    // ─── Consumer Factory ────────────────────────────────────────────────────

    @Bean
    public ConsumerFactory<String, AuditLogRequest> auditConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // ErrorHandlingDeserializer wraps JsonDeserializer so a single
        // bad/unparseable message doesn't kill the consumer thread.
        // It sets value=null + exception header on failure; our listener
        // checks for null and logs it explicitly.
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JacksonJsonDeserializer.class);

        // The producer (audit-starter) sends AuditEvent with a __TypeId__ header
        // pointing to com.example.auditstarter.model.AuditEvent — a class that
        // does NOT exist on audit-service's classpath. Ignore that header and
        // always map the JSON into AuditLogRequest (identical field layout).
        props.put(JacksonJsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        props.put(JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, AuditLogRequest.class.getName());
        props.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "*");

        return new DefaultKafkaConsumerFactory<>(props);
    }

    // ─── Dead-Letter Template ────────────────────────────────────────────────

    @Bean("auditDltKafkaTemplate")
    public KafkaTemplate<String, Object> auditDltKafkaTemplate() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));
    }

    // ─── Listener Container Factory ─────────────────────────────────────────

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AuditLogRequest> auditKafkaListenerContainerFactory(
            ConsumerFactory<String, AuditLogRequest> auditConsumerFactory,
            KafkaTemplate<String, Object> auditDltKafkaTemplate) {
        ConcurrentKafkaListenerContainerFactory<String, AuditLogRequest> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(auditConsumerFactory);
        factory.setCommonErrorHandler(buildErrorHandler(auditDltKafkaTemplate));
        return factory;
    }

    private DefaultErrorHandler buildErrorHandler(KafkaTemplate<String, Object> dltTemplate) {
        // Retry 3 times with exponential backoff, then route to DLT.
        ExponentialBackOff backOff = new ExponentialBackOff(500L, 2.0);
        backOff.setMaxAttempts(3);

        // Route failed messages to <topic>.DLT — always to partition 0 to
        // avoid needing DLT to have the same partition count as the source topic.
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(dltTemplate, (record, ex) -> {
            log.error(
                    "[AUDIT-CONSUMER] Message sent to DLT after retries exhausted "
                            + "[topic={}, partition={}, offset={}, key={}]: {}",
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    record.key(),
                    ex.getMessage());
            return new TopicPartition(record.topic() + ".DLT", 0);
        });

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);

        // Log every retry attempt so failures are visible at INFO, not silent.
        handler.setRetryListeners((record, ex, deliveryAttempt) -> log.warn(
                "[AUDIT-CONSUMER] Retry attempt {}/3 " + "[topic={}, partition={}, offset={}]: {}",
                deliveryAttempt,
                record.topic(),
                record.partition(),
                record.offset(),
                ex.getMessage()));

        return handler;
    }
}
