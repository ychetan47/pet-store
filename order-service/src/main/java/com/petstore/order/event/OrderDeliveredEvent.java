package com.petstore.order.event;

public class OrderDeliveredEvent {

    private Long orderId;
    private Long userId;

    public OrderDeliveredEvent() {
    }

    public OrderDeliveredEvent(Long orderId, Long userId) {
        this.orderId = orderId;
        this.userId = userId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
