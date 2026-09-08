package com.petstore.notification.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petstore.notification.metrics.NotificationMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Component
public class NotificationEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(NotificationEventConsumer.class);

    private final ObjectMapper objectMapper;
    private final NotificationMetrics notificationMetrics;

    // Thread-safe bounded LRU cache for idempotent event deduplication
    private final Set<String> processedEvents = Collections.newSetFromMap(
            new LinkedHashMap<String, Boolean>(1000, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
                    return size() > 5000;
                }
            }
    );

    public NotificationEventConsumer(ObjectMapper objectMapper, NotificationMetrics notificationMetrics) {
        this.objectMapper = objectMapper;
        this.notificationMetrics = notificationMetrics;
    }

    @KafkaListener(topics = "${app.kafka.topics.order-events:order.events}", groupId = "notification-service-group")
    public void handleOrderNotification(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);

            String eventId = root.path("eventId").asText(null);
            String eventType = root.path("eventType").asText(null);
            String correlationId = root.path("correlationId").asText(null);
            JsonNode payload = root.path("payload");

            if (correlationId != null) {
                MDC.put("correlationId", correlationId);
            }

            if (eventId != null && !processedEvents.add(eventId)) {
                logger.info("Notification event {} already processed. Skipping duplicate notification.", eventId);
                return;
            }

            String orderId = payload.path("orderId").asText(root.path("aggregateId").asText("N/A"));

            switch (eventType != null ? eventType : "") {
                case "OrderCreated":
                    String totalAmount = payload.path("totalAmount").asText("0.00");
                    String customerName = payload.path("shippingName").asText("Valued Customer");
                    logger.info("🔔 [NOTIFICATION] Order #{} created for {} with total amount ₹{}. Awaiting inventory confirmation.",
                            orderId, customerName, totalAmount);
                    break;

                case "OrderConfirmed":
                    logger.info("✅ [NOTIFICATION] Order #{} has been CONFIRMED! Inventory successfully reserved and order queued for dispatch.",
                            orderId);
                    break;

                case "OrderCancelled":
                    String reason = payload.path("reason").asText("Order cancelled");
                    logger.info("❌ [NOTIFICATION] Order #{} has been CANCELLED. Reason: {}", orderId, reason);
                    break;

                case "OrderShipped":
                    logger.info("🚚 [NOTIFICATION] Order #{} has been SHIPPED and is in transit to destination!", orderId);
                    break;

                case "OrderDelivered":
                    logger.info("🎉 [NOTIFICATION] Order #{} has been DELIVERED! Thank you for shopping with Paws & Claws.", orderId);
                    break;

                default:
                    logger.debug("Received unhandled event type in notification service: {}", eventType);
                    break;
            }

            notificationMetrics.incrementProcessed();

        } catch (Exception ex) {
            notificationMetrics.incrementFailed();
            logger.error("Failed to parse and log notification event from Kafka. Message: {}", message, ex);
        } finally {
            MDC.remove("correlationId");
        }
    }

    @KafkaListener(topics = "${app.kafka.topics.order-events-dlt:order.events.DLT}", groupId = "notification-dlt-group")
    public void handleDltNotification(String message) {
        logger.error("🚨 [NOTIFICATION DLT] Received dead-lettered event: {}", message);
        notificationMetrics.incrementDlt();
    }

    public boolean hasProcessed(String eventId) {
        return processedEvents.contains(eventId);
    }
}
