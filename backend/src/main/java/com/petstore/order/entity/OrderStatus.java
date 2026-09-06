package com.petstore.order.entity;

public enum OrderStatus {
    PLACED,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    public boolean canCancel() {
        return this == PLACED || this == CONFIRMED;
    }
}
