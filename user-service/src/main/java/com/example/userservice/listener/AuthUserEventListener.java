package com.example.userservice.listener;

import com.example.userservice.event.AuthUserEvent;
import com.example.userservice.service.UserEventProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthUserEventListener {

    private final UserEventProcessorService eventProcessorService;

    @KafkaListener(topics = "${app.kafka.topics.auth-user-events}", containerFactory = "kafkaListenerContainerFactory")
    public void onAuthUserEvent(AuthUserEvent event) {
        log.info("Received event {} type={} userId={}", event.getEventId(), event.getEventType(), event.getUserId());
        try {
            eventProcessorService.process(event);
        } catch (Exception ex) {
            // Let it surface to the container's error handler / DLQ policy rather than
            // silently swallowing — a failed apply must not be treated as processed.
            log.error(
                    "Failed to process auth event {} ({}): {}",
                    event.getEventId(),
                    event.getEventType(),
                    ex.getMessage(),
                    ex);
            throw ex;
        }
    }
}
