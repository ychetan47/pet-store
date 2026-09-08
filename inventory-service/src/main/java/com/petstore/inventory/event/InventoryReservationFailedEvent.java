package com.petstore.inventory.event;

import java.util.List;

public class InventoryReservationFailedEvent {

    private Long orderId;
    private String reason;
    private List<FailedItemPayload> failedItems;

    public InventoryReservationFailedEvent() {
    }

    public InventoryReservationFailedEvent(Long orderId, String reason, List<FailedItemPayload> failedItems) {
        this.orderId = orderId;
        this.reason = reason;
        this.failedItems = failedItems;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public List<FailedItemPayload> getFailedItems() {
        return failedItems;
    }

    public void setFailedItems(List<FailedItemPayload> failedItems) {
        this.failedItems = failedItems;
    }

    public static class FailedItemPayload {
        private Long productId;
        private Integer requestedQuantity;
        private Integer availableQuantity;

        public FailedItemPayload() {
        }

        public FailedItemPayload(Long productId, Integer requestedQuantity, Integer availableQuantity) {
            this.productId = productId;
            this.requestedQuantity = requestedQuantity;
            this.availableQuantity = availableQuantity;
        }

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public Integer getRequestedQuantity() {
            return requestedQuantity;
        }

        public void setRequestedQuantity(Integer requestedQuantity) {
            this.requestedQuantity = requestedQuantity;
        }

        public Integer getAvailableQuantity() {
            return availableQuantity;
        }

        public void setAvailableQuantity(Integer availableQuantity) {
            this.availableQuantity = availableQuantity;
        }
    }
}
