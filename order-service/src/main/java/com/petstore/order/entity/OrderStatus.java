package com.petstore.order.entity;

public enum OrderStatus {
    PLACED,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    public boolean isValidTransition(OrderStatus next) {
        if (this == next) return true;
        return switch (this) {
            case PLACED -> next == CONFIRMED || next == CANCELLED;
            case CONFIRMED -> next == SHIPPED || next == CANCELLED;
            case SHIPPED -> next == DELIVERED;
            case DELIVERED, CANCELLED -> false; // Terminal states
        };
    }

    public boolean isCancellable() {
        return this == PLACED || this == CONFIRMED;
    }
}
