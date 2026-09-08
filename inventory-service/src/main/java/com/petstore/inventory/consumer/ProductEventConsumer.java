package com.petstore.inventory.consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petstore.inventory.entity.ProcessedEvent;
import com.petstore.inventory.event.EventEnvelope;
import com.petstore.inventory.event.ProductCreatedEvent;
import com.petstore.inventory.repository.ProcessedEventRepository;
import com.petstore.inventory.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ProductEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(ProductEventConsumer.class);

    private final InventoryService inventoryService;
    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper;

    public ProductEventConsumer(InventoryService inventoryService,
                                ProcessedEventRepository processedEventRepository,
                                ObjectMapper objectMapper) {
        this.inventoryService = inventoryService;
        this.processedEventRepository = processedEventRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.kafka.topics.product-events:product.events}", groupId = "inventory-service-group")
    @Transactional
    public void handleProductEvent(String message) {
        try {
            EventEnvelope<Object> rawEnvelope = objectMapper.readValue(message, new TypeReference<EventEnvelope<Object>>() {});

            String correlationId = rawEnvelope.getCorrelationId();
            if (correlationId != null) {
                MDC.put("correlationId", correlationId);
            }

            String eventId = rawEnvelope.getEventId();
            String eventType = rawEnvelope.getEventType();

            if (processedEventRepository.existsByEventId(eventId)) {
                logger.info("Product event {} ({}) already processed. Skipping duplicate.", eventId, eventType);
                return;
            }

            if ("ProductCreated".equals(eventType)) {
                EventEnvelope<ProductCreatedEvent> envelope = objectMapper.readValue(
                        message, new TypeReference<EventEnvelope<ProductCreatedEvent>>() {}
                );
                ProductCreatedEvent payload = envelope.getPayload();
                logger.info("Processing ProductCreated event {} for product id {}", eventId, payload.getProductId());

                inventoryService.createItem(payload.getProductId(), payload.getInitialStock());
                processedEventRepository.save(new ProcessedEvent(eventId, eventType));
            } else {
                logger.debug("Ignoring product event type: {}", eventType);
            }

        } catch (Exception ex) {
            logger.error("Failed to process product event from Kafka. Message: {}", message, ex);
            throw new RuntimeException("Error processing product event in inventory-service", ex);
        } finally {
            MDC.remove("correlationId");
        }
    }
}
