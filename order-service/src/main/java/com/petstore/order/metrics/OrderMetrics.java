package com.petstore.order.metrics;

import com.petstore.order.outbox.OutboxRepository;
import com.petstore.order.outbox.OutboxStatus;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class OrderMetrics {

    private final Counter ordersCreatedCounter;
    private final Counter ordersConfirmedCounter;
    private final Counter ordersCancelledCounter;
    private final Counter dltMessagesCounter;
    private final Counter outboxCreatedCounter;
    private final Counter outboxPublishedCounter;
    private final Counter outboxFailedCounter;

    public OrderMetrics(MeterRegistry registry, OutboxRepository outboxRepository) {
        this.ordersCreatedCounter = Counter.builder("orders_created_total")
                .description("Total number of orders placed/created")
                .register(registry);

        this.ordersConfirmedCounter = Counter.builder("orders_confirmed_total")
                .description("Total number of orders confirmed following inventory reservation")
                .register(registry);

        this.ordersCancelledCounter = Counter.builder("orders_cancelled_total")
                .description("Total number of orders cancelled")
                .register(registry);

        this.dltMessagesCounter = Counter.builder("dlt_messages_total")
                .description("Total number of messages routed to Dead Letter Topic")
                .tag("service", "order-service")
                .register(registry);

        this.outboxCreatedCounter = Counter.builder("outbox_events_created_total")
                .description("Total number of outbox events recorded")
                .register(registry);

        this.outboxPublishedCounter = Counter.builder("outbox_events_published_total")
                .description("Total number of outbox events successfully published")
                .register(registry);

        this.outboxFailedCounter = Counter.builder("outbox_events_failed_total")
                .description("Total number of outbox events failed to publish")
                .register(registry);

        Gauge.builder("outbox_pending_events", outboxRepository, repo -> (double) repo.countByStatus(OutboxStatus.PENDING))
                .description("Number of pending events in transactional outbox")
                .register(registry);
    }

    public void incrementOrdersCreated() {
        ordersCreatedCounter.increment();
    }

    public void incrementOrdersConfirmed() {
        ordersConfirmedCounter.increment();
    }

    public void incrementOrdersCancelled() {
        ordersCancelledCounter.increment();
    }

    public void incrementDltMessages() {
        dltMessagesCounter.increment();
    }

    public void incrementOutboxCreated() {
        outboxCreatedCounter.increment();
    }

    public void incrementOutboxPublished() {
        outboxPublishedCounter.increment();
    }

    public void incrementOutboxFailed() {
        outboxFailedCounter.increment();
    }
}
