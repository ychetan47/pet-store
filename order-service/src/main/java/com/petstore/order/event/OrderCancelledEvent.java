package com.petstore.order.event;

public class OrderCancelledEvent {

    private Long orderId;
    private Long userId;
    private String reason;

    public OrderCancelledEvent() {
    }

    public OrderCancelledEvent(Long orderId, Long userId, String reason) {
        this.orderId = orderId;
        this.userId = userId;
        this.reason = reason;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
