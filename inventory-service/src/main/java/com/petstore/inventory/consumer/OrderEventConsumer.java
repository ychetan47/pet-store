package com.petstore.inventory.consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petstore.inventory.entity.ProcessedEvent;
import com.petstore.inventory.event.EventEnvelope;
import com.petstore.inventory.event.OrderCancelledEvent;
import com.petstore.inventory.event.OrderCreatedEvent;
import com.petstore.inventory.repository.ProcessedEventRepository;
import com.petstore.inventory.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OrderEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final InventoryService inventoryService;
    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper;

    public OrderEventConsumer(InventoryService inventoryService,
                              ProcessedEventRepository processedEventRepository,
                              ObjectMapper objectMapper) {
        this.inventoryService = inventoryService;
        this.processedEventRepository = processedEventRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.kafka.topics.order-events:order.events}", groupId = "inventory-service-group")
    @Transactional
    public void handleOrderEvent(String message) {
        try {
            // Read root envelope metadata first
            EventEnvelope<Object> rawEnvelope = objectMapper.readValue(message, new TypeReference<EventEnvelope<Object>>() {});

            String correlationId = rawEnvelope.getCorrelationId();
            if (correlationId != null) {
                MDC.put("correlationId", correlationId);
            }

            String eventId = rawEnvelope.getEventId();
            String eventType = rawEnvelope.getEventType();

            // Idempotency check: ignore if already processed
            if (processedEventRepository.existsByEventId(eventId)) {
                logger.info("Event {} ({}) has already been processed by inventory-service. Skipping duplicate.",
                        eventId, eventType);
                return;
            }

            if ("OrderCreated".equals(eventType)) {
                EventEnvelope<OrderCreatedEvent> envelope = objectMapper.readValue(
                        message, new TypeReference<EventEnvelope<OrderCreatedEvent>>() {}
                );
                OrderCreatedEvent payload = envelope.getPayload();
                logger.info("Processing OrderCreated event {} for order {}", eventId, payload.getOrderId());

                inventoryService.reserveStock(payload.getOrderId(), payload.getItems(), correlationId);
                processedEventRepository.save(new ProcessedEvent(eventId, eventType));

            } else if ("OrderCancelled".equals(eventType)) {
                EventEnvelope<OrderCancelledEvent> envelope = objectMapper.readValue(
                        message, new TypeReference<EventEnvelope<OrderCancelledEvent>>() {}
                );
                OrderCancelledEvent payload = envelope.getPayload();
                logger.info("Processing OrderCancelled event {} for order {}", eventId, payload.getOrderId());

                inventoryService.releaseStock(payload.getOrderId(), correlationId);
                processedEventRepository.save(new ProcessedEvent(eventId, eventType));

            } else {
                logger.debug("Ignoring irrelevant order event type: {}", eventType);
            }

        } catch (Exception ex) {
            logger.error("Failed to process order event from Kafka. Message: {}", message, ex);
            throw new RuntimeException("Error processing order event in inventory-service", ex);
        } finally {
            MDC.remove("correlationId");
        }
    }
}
