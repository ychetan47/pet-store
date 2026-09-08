package com.petstore.order.outbox;

public enum OutboxStatus {
    PENDING,
    PUBLISHED,
    FAILED
}
