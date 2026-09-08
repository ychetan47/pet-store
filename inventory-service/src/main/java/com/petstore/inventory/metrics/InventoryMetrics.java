package com.petstore.inventory.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class InventoryMetrics {

    private final Counter reservationsCounter;
    private final Counter reservationFailuresCounter;
    private final Counter releasesCounter;
    private final Counter dltMessagesCounter;

    public InventoryMetrics(MeterRegistry registry) {
        this.reservationsCounter = Counter.builder("inventory_reservations_total")
                .description("Total number of successful inventory stock reservations")
                .register(registry);

        this.reservationFailuresCounter = Counter.builder("inventory_reservation_failures_total")
                .description("Total number of failed inventory stock reservations")
                .register(registry);

        this.releasesCounter = Counter.builder("inventory_releases_total")
                .description("Total number of inventory stock releases")
                .register(registry);

        this.dltMessagesCounter = Counter.builder("dlt_messages_total")
                .description("Total number of messages routed to Dead Letter Topic")
                .tag("service", "inventory-service")
                .register(registry);
    }

    public void incrementReservations() {
        reservationsCounter.increment();
    }

    public void incrementReservationFailures() {
        reservationFailuresCounter.increment();
    }

    public void incrementReleases() {
        releasesCounter.increment();
    }

    public void incrementDltMessages() {
        dltMessagesCounter.increment();
    }
}
