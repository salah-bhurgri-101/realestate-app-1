package com.realestate.auditservice.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${audit.kafka.topic:audit-events}")
    private String topicName;

    @Value("${audit.kafka.partitions:3}")
    private int partitions;

    @Value("${audit.kafka.replicas:1}")
    private short replicas;

    @Bean
    public NewTopic auditEventsTopic() {
        return TopicBuilder.name(topicName)
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }

    @Bean
    public NewTopic auditEventsDeadLetterTopic() {
        // DeadLetterPublishingRecoverer's default naming convention: <topic>.DLT
        return TopicBuilder.name(topicName + ".DLT")
                .partitions(1)
                .replicas(replicas)
                .build();
    }
}
