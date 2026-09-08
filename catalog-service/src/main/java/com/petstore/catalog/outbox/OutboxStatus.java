package com.petstore.catalog.outbox;

public enum OutboxStatus {
    PENDING,
    PUBLISHED,
    FAILED
}
