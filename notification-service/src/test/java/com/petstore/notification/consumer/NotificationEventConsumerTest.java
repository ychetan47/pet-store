package com.petstore.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petstore.notification.metrics.NotificationMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationEventConsumerTest {

    private NotificationEventConsumer consumer;
    private ObjectMapper objectMapper;
    private NotificationMetrics notificationMetrics;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        notificationMetrics = new NotificationMetrics(new SimpleMeterRegistry());
        consumer = new NotificationEventConsumer(objectMapper, notificationMetrics);
    }

    @Test
    void testHandleOrderCreatedNotification_Idempotent() {
        String json = """
        {
          "eventId": "test-event-101",
          "eventType": "OrderCreated",
          "aggregateId": "1001",
          "aggregateType": "ORDER",
          "eventVersion": 1,
          "occurredAt": "2026-09-08T22:00:00Z",
          "correlationId": "corr-101",
          "payload": {
            "orderId": 1001,
            "totalAmount": 2850.00,
            "shippingName": "Rahul Sharma"
          }
        }
        """;

        assertFalse(consumer.hasProcessed("test-event-101"));

        // First execution
        consumer.handleOrderNotification(json);
        assertTrue(consumer.hasProcessed("test-event-101"));

        // Duplicate execution (should be recognized and ignored)
        consumer.handleOrderNotification(json);
        assertTrue(consumer.hasProcessed("test-event-101"));
    }

    @Test
    void testHandleOrderConfirmedNotification() {
        String json = """
        {
          "eventId": "test-event-102",
          "eventType": "OrderConfirmed",
          "aggregateId": "1001",
          "aggregateType": "ORDER",
          "payload": { "orderId": 1001 }
        }
        """;

        consumer.handleOrderNotification(json);
        assertTrue(consumer.hasProcessed("test-event-102"));
    }
}
