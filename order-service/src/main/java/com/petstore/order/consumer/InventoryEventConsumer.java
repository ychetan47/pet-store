package com.petstore.order.consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petstore.order.entity.Order;
import com.petstore.order.entity.OrderStatus;
import com.petstore.order.entity.ProcessedEvent;
import com.petstore.order.event.*;
import com.petstore.order.outbox.OutboxService;
import com.petstore.order.repository.OrderRepository;
import com.petstore.order.repository.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class InventoryEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(InventoryEventConsumer.class);

    private final OrderRepository orderRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final OutboxService outboxService;
    private final ObjectMapper objectMapper;
    private final com.petstore.order.metrics.OrderMetrics orderMetrics;

    public InventoryEventConsumer(OrderRepository orderRepository,
                                  ProcessedEventRepository processedEventRepository,
                                  OutboxService outboxService,
                                  ObjectMapper objectMapper,
                                  com.petstore.order.metrics.OrderMetrics orderMetrics) {
        this.orderRepository = orderRepository;
        this.processedEventRepository = processedEventRepository;
        this.outboxService = outboxService;
        this.objectMapper = objectMapper;
        this.orderMetrics = orderMetrics;
    }

    @KafkaListener(topics = "${app.kafka.topics.inventory-events:inventory.events}", groupId = "order-service-group")
    @Transactional
    public void handleInventoryEvent(String message) {
        try {
            EventEnvelope<Object> rawEnvelope = objectMapper.readValue(message, new TypeReference<EventEnvelope<Object>>() {});

            String correlationId = rawEnvelope.getCorrelationId();
            if (correlationId != null) {
                MDC.put("correlationId", correlationId);
            }

            String eventId = rawEnvelope.getEventId();
            String eventType = rawEnvelope.getEventType();

            if (processedEventRepository.existsByEventId(eventId)) {
                logger.info("Event {} ({}) already processed by order-service. Skipping duplicate.", eventId, eventType);
                return;
            }

            if ("InventoryReserved".equals(eventType)) {
                EventEnvelope<InventoryReservedEvent> envelope = objectMapper.readValue(
                        message, new TypeReference<EventEnvelope<InventoryReservedEvent>>() {}
                );
                Long orderId = envelope.getPayload().getOrderId();
                logger.info("Received InventoryReserved event {} for order {}", eventId, orderId);

                orderRepository.findById(orderId).ifPresent(order -> {
                    if (order.getOrderStatus() == OrderStatus.PLACED) {
                        order.setOrderStatus(OrderStatus.CONFIRMED);
                        orderRepository.save(order);
                        logger.info("Order {} transitioned from PLACED to CONFIRMED following inventory reservation.", orderId);

                        outboxService.recordEvent(
                                "OrderConfirmed",
                                orderId.toString(),
                                "ORDER",
                                1,
                                correlationId,
                                new OrderConfirmedEvent(orderId, order.getUserId(), order.getTotalAmount())
                        );
                        orderMetrics.incrementOrdersConfirmed();
                    }
                });

                processedEventRepository.save(new ProcessedEvent(eventId, eventType));

            } else if ("InventoryReservationFailed".equals(eventType)) {
                EventEnvelope<InventoryReservationFailedEvent> envelope = objectMapper.readValue(
                        message, new TypeReference<EventEnvelope<InventoryReservationFailedEvent>>() {}
                );
                Long orderId = envelope.getPayload().getOrderId();
                String reason = envelope.getPayload().getReason();
                logger.warn("Received InventoryReservationFailed event {} for order {}. Reason: {}", eventId, orderId, reason);

                orderRepository.findById(orderId).ifPresent(order -> {
                    if (order.getOrderStatus() == OrderStatus.PLACED) {
                        order.setOrderStatus(OrderStatus.CANCELLED);
                        orderRepository.save(order);
                        logger.info("Order {} transitioned to CANCELLED due to inventory reservation failure.", orderId);

                        outboxService.recordEvent(
                                "OrderCancelled",
                                orderId.toString(),
                                "ORDER",
                                1,
                                correlationId,
                                new OrderCancelledEvent(orderId, order.getUserId(), reason != null ? reason : "Insufficient stock")
                        );
                        orderMetrics.incrementOrdersCancelled();
                    }
                });

                processedEventRepository.save(new ProcessedEvent(eventId, eventType));

            } else {
                logger.debug("Ignoring irrelevant inventory event type: {}", eventType);
            }

        } catch (Exception ex) {
            logger.error("Failed to process inventory event from Kafka: {}", message, ex);
            throw new RuntimeException("Error processing inventory event in order-service", ex);
        } finally {
            MDC.remove("correlationId");
        }
    }
}
