package com.petstore.order.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petstore.order.event.EventEnvelope;
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
    private final com.petstore.order.metrics.OrderMetrics orderMetrics;

    public OutboxService(OutboxRepository outboxRepository,
                         ObjectMapper objectMapper,
                         com.petstore.order.metrics.OrderMetrics orderMetrics) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
        this.orderMetrics = orderMetrics;
    }

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
            orderMetrics.incrementOutboxCreated();
            logger.debug("Enqueued outbox event {} for aggregate {} (type: {})",
                    envelope.getEventId(), aggregateId, eventType);
            return saved;
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize event payload for aggregate {}", aggregateId, e);
            throw new RuntimeException("Failed to serialize outbox event", e);
        }
    }
}
