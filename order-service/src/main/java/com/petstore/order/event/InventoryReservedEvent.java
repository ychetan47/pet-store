package com.petstore.order.event;

public class InventoryReservedEvent {

    private Long orderId;

    public InventoryReservedEvent() {
    }

    public InventoryReservedEvent(Long orderId) {
        this.orderId = orderId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
}
