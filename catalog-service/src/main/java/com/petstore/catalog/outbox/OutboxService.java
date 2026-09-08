package com.petstore.catalog.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petstore.catalog.event.EventEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
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

    @Transactional
    public <T> OutboxEvent recordEvent(String eventType, String aggregateId, String aggregateType,
                                       Integer eventVersion, String correlationId, T payload) {
        try {
            EventEnvelope<T> envelope = new EventEnvelope<>(
                    eventType,
                    aggregateId,
                    aggregateType,
                    eventVersion != null ? eventVersion : 1,
                    correlationId,
                    payload
            );

            String jsonPayload = objectMapper.writeValueAsString(envelope);

            OutboxEvent outboxEvent = new OutboxEvent(
                    envelope.getEventId(),
                    aggregateType,
                    aggregateId,
                    eventType,
                    envelope.getEventVersion(),
                    jsonPayload,
                    correlationId
            );

            OutboxEvent saved = outboxRepository.save(outboxEvent);
            logger.info("Recorded outbox event {} of type {} for aggregate {}/{}",
                    saved.getEventId(), eventType, aggregateType, aggregateId);
            return saved;
        } catch (Exception ex) {
            logger.error("Failed to serialize outbox event payload for type {}", eventType, ex);
            throw new RuntimeException("Error saving outbox event", ex);
        }
    }
}
