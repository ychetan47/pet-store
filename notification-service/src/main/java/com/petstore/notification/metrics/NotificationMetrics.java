package com.petstore.notification.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class NotificationMetrics {

    private final Counter processedCounter;
    private final Counter failedCounter;
    private final Counter dltCounter;

    public NotificationMetrics(MeterRegistry registry) {
        this.processedCounter = Counter.builder("notifications_processed_total")
                .description("Total number of successfully processed notifications")
                .register(registry);

        this.failedCounter = Counter.builder("notifications_failed_total")
                .description("Total number of failed notification deliveries/processes")
                .register(registry);

        this.dltCounter = Counter.builder("dlt_messages_total")
                .description("Total number of messages routed to Dead Letter Topic")
                .tag("service", "notification-service")
                .register(registry);
    }

    public void incrementProcessed() {
        processedCounter.increment();
    }

    public void incrementFailed() {
        failedCounter.increment();
    }

    public void incrementDlt() {
        dltCounter.increment();
    }
}
