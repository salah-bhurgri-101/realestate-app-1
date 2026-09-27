package com.realestate.auth_service.service;

import com.realestate.auth_service.dto.request.RegisterRequest;
import com.realestate.auth_service.entity.User;
import com.realestate.auth_service.event.AuthUserEvent;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthUserEventPublisher {

    private final KafkaTemplate<String, AuthUserEvent> kafkaTemplate;

    @Value("${auth.kafka.user-events-topic}")
    private String topic;

    public void publish(User user, String eventType) {
        publish(user, eventType, null);
    }

    public void publish(User user, String eventType, RegisterRequest profileData) {
        AuthUserEvent event = AuthUserEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .displayName(profileData != null ? profileData.getName() : user.getName())
                .bio(profileData != null ? profileData.getBio() : null)
                .avatarUrl(profileData != null ? profileData.getAvatarUrl() : null)
                .coverUrl(profileData != null ? profileData.getCoverUrl() : null)
                .dateOfBirth(profileData != null ? profileData.getDateOfBirth() : null)
                .gender(profileData != null ? profileData.getGender() : null)
                .phone(user.getPhone())
                .occurredAt(LocalDateTime.now())
                .build();

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send(event);
                }
            });
            return;
        }

        send(event);
    }

    private void send(AuthUserEvent event) {
        kafkaTemplate.send(topic, event.getUserId().toString(), event).whenComplete((result, error) -> {
            if (error != null) {
                log.error(
                        "Failed to publish auth user event {} for user {}",
                        event.getEventType(),
                        event.getUserId(),
                        error);
            } else {
                log.debug("Published auth user event {} for user {}", event.getEventType(), event.getUserId());
            }
        });
    }
}
