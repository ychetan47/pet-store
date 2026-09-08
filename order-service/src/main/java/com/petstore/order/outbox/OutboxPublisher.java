package com.petstore.order.outbox;

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
    private final com.petstore.order.metrics.OrderMetrics orderMetrics;

    @Value("${app.kafka.topics.order-events:order.events}")
    private String orderEventsTopic;

    public OutboxPublisher(OutboxRepository outboxRepository,
                           KafkaTemplate<String, String> kafkaTemplate,
                           com.petstore.order.metrics.OrderMetrics orderMetrics) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.orderMetrics = orderMetrics;
    }

    @Scheduled(fixedDelay = 500)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxRepository.findPendingForUpdateSkipLocked(50);
        if (pendingEvents.isEmpty()) {
            return;
        }

        for (OutboxEvent event : pendingEvents) {
            String topic = orderEventsTopic;
            String key = event.getAggregateId();

            try {
                kafkaTemplate.send(topic, key, event.getPayload())
                        .whenComplete((result, ex) -> {
                            if (ex == null) {
                                logger.debug("Successfully dispatched outbox event {} to topic {}", event.getEventId(), topic);
                            } else {
                                logger.error("Failed to dispatch outbox event {} to topic {}", event.getEventId(), topic, ex);
                            }
                        });

                event.setStatus(OutboxStatus.PUBLISHED);
                event.setPublishedAt(Instant.now());
                outboxRepository.save(event);
                orderMetrics.incrementOutboxPublished();
            } catch (Exception ex) {
                logger.error("Error publishing outbox event {}", event.getEventId(), ex);
                event.setRetryCount(event.getRetryCount() + 1);
                event.setLastError(ex.getMessage());
                if (event.getRetryCount() >= 5) {
                    event.setStatus(OutboxStatus.FAILED);
                    orderMetrics.incrementOutboxFailed();
                }
                outboxRepository.save(event);
            }
        }
    }
}
