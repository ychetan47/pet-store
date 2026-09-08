package com.petstore.inventory.outbox;

import com.petstore.inventory.entity.OutboxEvent;
import com.petstore.inventory.entity.OutboxStatus;
import com.petstore.inventory.repository.OutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class OutboxPublisher {

    private static final Logger logger = LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${app.kafka.topics.inventory-events:inventory.events}")
    private String inventoryEventsTopic;

    public OutboxPublisher(OutboxRepository outboxRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 500)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxRepository.findPendingForUpdateSkipLocked(50);
        if (pendingEvents.isEmpty()) {
            return;
        }

        for (OutboxEvent event : pendingEvents) {
            String topic = inventoryEventsTopic;
            String key = event.getAggregateId();

            try {
                kafkaTemplate.send(topic, key, event.getPayload())
                        .whenComplete((result, ex) -> {
                            if (ex == null) {
                                logger.debug("Successfully published outbox event {} to topic {}", event.getEventId(), topic);
                            } else {
                                logger.error("Failed to publish outbox event {} to topic {}", event.getEventId(), topic, ex);
                            }
                        });

                event.setStatus(OutboxStatus.PUBLISHED);
                event.setPublishedAt(Instant.now());
                outboxRepository.save(event);
            } catch (Exception ex) {
                logger.error("Synchronous error dispatching outbox event {}", event.getEventId(), ex);
                event.setRetryCount(event.getRetryCount() + 1);
                event.setLastError(ex.getMessage());
                if (event.getRetryCount() >= 5) {
                    event.setStatus(OutboxStatus.FAILED);
                }
                outboxRepository.save(event);
            }
        }
    }
}
