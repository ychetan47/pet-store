package com.petstore.inventory.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petstore.inventory.entity.OutboxEvent;
import com.petstore.inventory.event.EventEnvelope;
import com.petstore.inventory.repository.OutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OutboxService {

    private static final Logger logger = LoggerFactory.getLogger(OutboxService.class);

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public OutboxService(OutboxRepository outboxRepository, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Enqueues an event into outbox_events within the current active database transaction.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public <T> OutboxEvent recordEvent(String eventType, String aggregateId, String aggregateType,
                                       int eventVersion, String correlationId, T payload) {
        try {
            EventEnvelope<T> envelope = new EventEnvelope<>(
                    eventType, aggregateId, aggregateType, eventVersion, correlationId, payload
            );
            String jsonPayload = objectMapper.writeValueAsString(envelope);

            OutboxEvent outboxEvent = new OutboxEvent(
                    envelope.getEventId(),
                    aggregateType,
                    aggregateId,
                    eventType,
                    eventVersion,
                    jsonPayload,
                    correlationId
            );

            OutboxEvent saved = outboxRepository.save(outboxEvent);
            logger.debug("Enqueued outbox event {} for aggregate {} (type: {})",
                    envelope.getEventId(), aggregateId, eventType);
            return saved;
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize event payload for aggregate {}", aggregateId, e);
            throw new RuntimeException("Failed to serialize outbox event", e);
        }
    }
}
